package yunrry.flik.adapters.out.persistence.mysql.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(name = "sig_eng_nm")
    private String sigEngNm;

    @Column(name = "sig_kor_nm")
    private String sigKorNm;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
