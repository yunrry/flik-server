//package yunrry.flik.entity;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "emd_data",
//    uniqueConstraints = {
//        @UniqueConstraint(name = "uk_emd_cd", columnNames = "emdCd")
//    },
//    indexes = {
//        @Index(name = "idx_emd_cd", columnList = "emdCd"),
//        @Index(name = "idx_emd_kor_nm", columnList = "emdKorNm"),
//        @Index(name = "idx_emd_eng_nm", columnList = "emdEngNm")
//    })
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class EmdData {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "EMD_CD", nullable = false, length = 20)
//    private String emdCd;
//
//    @Column(name = "EMD_ENG_NM", nullable = false, length = 200)
//    private String emdEngNm;
//
//    @Column(name = "EMD_KOR_NM", nullable = false, length = 200)
//    private String emdKorNm;
//
//    @Column(name = "Polygon", nullable = false, columnDefinition = "JSON")
//    private String polygon;
//
//    @CreationTimestamp
//    @Column(name = "created_at")
//    private LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;
//}