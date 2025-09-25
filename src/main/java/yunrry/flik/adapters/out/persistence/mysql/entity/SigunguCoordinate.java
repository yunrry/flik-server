package yunrry.flik.adapters.out.persistence.mysql.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.geo.Point;

import java.time.LocalDateTime;

@Entity
@Table(name = "sigungu_coordinate")
@Getter
@NoArgsConstructor
public class SigunguCoordinate {
    @Id
    @Column(name = "sig_cd")
    private String sigCd;

    @Column(name = "x")
    private Double x;

    @Column(name = "y")
    private Double y;

    // 공간 인덱스용 컬럼 추가
    @Column(name = "location", columnDefinition = "POINT")
    private Point location;

    @Column(name = "sig_eng_nm")
    private String sigEngNm;

    @Column(name = "sig_kor_nm")
    private String sigKorNm;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
