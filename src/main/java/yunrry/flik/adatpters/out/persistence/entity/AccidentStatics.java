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
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "accident_statics", indexes = {
//    @Index(name = "idx_year", columnList = "accidentYear"),
//    @Index(name = "idx_sido", columnList = "sidoCode"),
//    @Index(name = "idx_accident_type", columnList = "accidentTypeName")
//})
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class AccidentStatics {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "accident_year", nullable = false)
//    private Integer accidentYear;
//
//    @Column(name = "accident_type_name", nullable = false, length = 50)
//    private String accidentTypeName;
//
//    @Column(name = "sido_code", nullable = false, length = 10)
//    private String sidoCode;
//
//    @Column(name = "sido_name", length = 50)
//    private String sidoName;
//
//    @Column(name = "accident_count")
//    private Integer accidentCount;
//
//    @Column(name = "accident_count_ratio", precision = 5, scale = 2)
//    private BigDecimal accidentCountRatio;
//
//    @Column(name = "death_count")
//    private Integer deathCount;
//
//    @Column(name = "death_count_ratio", precision = 5, scale = 2)
//    private BigDecimal deathCountRatio;
//
//    @Column(name = "fatality_rate", precision = 5, scale = 2)
//    private BigDecimal fatalityRate;
//
//    @Column(name = "injury_count")
//    private Integer injuryCount;
//
//    @Column(name = "injury_count_ratio", precision = 5, scale = 2)
//    private BigDecimal injuryCountRatio;
//
//    @Column(name = "accident_per_100k_population", precision = 10, scale = 2)
//    private BigDecimal accidentPer100kPopulation;
//
//    @Column(name = "accident_per_10k_vehicles", precision = 10, scale = 2)
//    private BigDecimal accidentPer10kVehicles;
//
//    @Column(name = "speeding_count")
//    private Integer speedingCount;
//
//    @Column(name = "center_line_violation_count")
//    private Integer centerLineViolationCount;
//
//    @Column(name = "signal_violation_count")
//    private Integer signalViolationCount;
//
//    @Column(name = "unsafe_distance_count")
//    private Integer unsafeDistanceCount;
//
//    @Column(name = "unsafe_driving_count")
//    private Integer unsafeDrivingCount;
//
//    @Column(name = "pedestrian_protection_violation_count")
//    private Integer pedestrianProtectionViolationCount;
//
//    @Column(name = "other_violation_count")
//    private Integer otherViolationCount;
//
//    @Column(name = "vehicle_pedestrian_accident_count")
//    private Integer vehiclePedestrianAccidentCount;
//
//    @Column(name = "vehicle_vehicle_accident_count")
//    private Integer vehicleVehicleAccidentCount;
//
//    @Column(name = "single_vehicle_accident_count")
//    private Integer singleVehicleAccidentCount;
//
//    @Column(name = "railroad_crossing_accident_count")
//    private Integer railroadCrossingAccidentCount;
//
//    @CreationTimestamp
//    @Column(name = "created_at")
//    private LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;
//}