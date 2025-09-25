#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import csv
import os
from typing import Dict, List, Tuple, Optional

import mysql.connector
from mysql.connector import Error
from dotenv import load_dotenv

load_dotenv()

class SigunguCoordinateMigrator:
    def __init__(self, db_config: Dict[str, str], csv_path: str, table_name: str = "sigungu_coordinate"):
        self.db_config = db_config
        self.csv_path = csv_path
        self.table_name = table_name

    def _connect(self):
        return mysql.connector.connect(**self.db_config)

    def create_table(self):
        conn = self._connect()
        cursor = conn.cursor()
        try:
            create_table_sql = f"""
                CREATE TABLE IF NOT EXISTS {self.table_name} (
                    sig_cd VARCHAR(10) NOT NULL PRIMARY KEY,
                    x DOUBLE NOT NULL,
                    y DOUBLE NOT NULL,
                    sig_eng_nm VARCHAR(100) NOT NULL,
                    sig_kor_nm VARCHAR(100) NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_sig_eng_nm (sig_eng_nm),
                    INDEX idx_sig_kor_nm (sig_kor_nm)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """
            cursor.execute(create_table_sql)
            conn.commit()
            print(f"✓ {self.table_name} 테이블 생성 완료")
        except Exception as e:
            print(f"✗ 테이블 생성 실패: {e}")
            conn.rollback()
            raise
        finally:
            cursor.close()
            conn.close()

    def transform_sig_cd(self, sig_cd: str) -> str:
        s = sig_cd.strip()
        if s.startswith("42"):
            return "51" + s[2:]
        if s.startswith("45"):
            return "52" + s[2:]
        return s

    def _parse_row(self, row: Dict[str, str]) -> Optional[Tuple[str, float, float, str, str]]:
        try:
            x = float(row["X"])
            y = float(row["Y"])
            sig_cd = self.transform_sig_cd(str(row["SIG_CD"]))
            sig_eng = str(row["SIG_ENG_NM"]).strip()
            sig_kor = str(row["SIG_KOR_NM"]).strip()
            if not sig_cd or not sig_eng or not sig_kor:
                return None
            return (sig_cd, x, y, sig_eng, sig_kor)
        except Exception:
            return None

    def load_csv_rows(self) -> List[Tuple[str, float, float, str, str]]:
        rows: List[Tuple[str, float, float, str, str]] = []
        with open(self.csv_path, "r", encoding="utf-8-sig", newline="") as f:
            reader = csv.DictReader(f)
            for row in reader:
                parsed = self._parse_row(row)
                if parsed:
                    rows.append(parsed)
        print(f"✓ CSV 로드 및 파싱 완료: {len(rows)}개 행")
        return rows

    def upsert_rows(self, rows: List[Tuple[str, float, float, str, str]], batch_size: int = 500):
        if not rows:
            print("⚠ 업서트할 데이터가 없습니다.")
            return

        conn = self._connect()
        cursor = conn.cursor()
        try:
            upsert_sql = f"""
                INSERT INTO {self.table_name} (sig_cd, x, y, sig_eng_nm, sig_kor_nm)
                VALUES (%s, %s, %s, %s, %s)
                ON DUPLICATE KEY UPDATE
                    x = VALUES(x),
                    y = VALUES(y),
                    sig_eng_nm = VALUES(sig_eng_nm),
                    sig_kor_nm = VALUES(sig_kor_nm)
            """
            for i in range(0, len(rows), batch_size):
                batch = rows[i:i + batch_size]
                cursor.executemany(upsert_sql, batch)
                conn.commit()
            print(f"✓ 업서트 완료: {len(rows)}개 행")
        except Error as e:
            print(f"✗ 업서트 실패: {e}")
            conn.rollback()
            raise
        finally:
            cursor.close()
            conn.close()

    def run(self):
        self.create_table()
        rows = self.load_csv_rows()
        self.upsert_rows(rows)


def main():
    db_config = {
        'host': os.getenv('MYSQL_HOST', 'localhost'),
        'user': os.getenv('MYSQL_USER', 'root'),
        'password': os.getenv('MYSQL_PASSWORD', ''),
        'database': os.getenv('MYSQL_DATABASE', 'flik_db'),
        'charset': 'utf8mb4'
    }

    # CSV 경로: 필요 시 직접 수정
    csv_path = os.getenv(
          'SIGUNGU_CSV_PATH',
        'sigungu_coordinate.csv'
    )

    migrator = SigunguCoordinateMigrator(db_config=db_config, csv_path=csv_path, table_name="sigungu_coordinate")
    migrator.run()


if __name__ == "__main__":
    main()