//package yunrry.flik.config;
//
//import com.zaxxer.hikari.HikariConfig;
//import com.zaxxer.hikari.HikariDataSource;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Profile;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.orm.jpa.JpaTransactionManager;
//import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
//import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
//import org.springframework.transaction.PlatformTransactionManager;
//
//import javax.sql.DataSource;
//import java.util.Properties;
//
//@Configuration
//@Profile("!test")
//@EnableJpaRepositories(
//        basePackages = "yunrry.flik.adapters.out.persistence.postgres",
//        entityManagerFactoryRef = "vectorEntityManagerFactory",
//        transactionManagerRef = "vectorTransactionManager"
//)
//public class VectorConfig {
//
//    @Value("${spring.datasource.vector.url}")
//    private String jdbcUrl;
//
//    @Value("${spring.datasource.vector.username}")
//    private String username;
//
//    @Value("${spring.datasource.vector.password}")
//    private String password;
//
//    @Value("${spring.datasource.vector.driver-class-name}")
//    private String driverClassName;
//
//    @Bean(name = "vectorDataSource")
//    public DataSource vectorDataSource() {
//        HikariConfig config = new HikariConfig();
//        config.setJdbcUrl(jdbcUrl);
//        config.setUsername(username);
//        config.setPassword(password);
//        config.setDriverClassName(driverClassName);
//        return new HikariDataSource(config);
//    }
//
//    @Bean(name = "vectorEntityManagerFactory")
//    public LocalContainerEntityManagerFactoryBean vectorEntityManagerFactory() {
//        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
//        factory.setDataSource(vectorDataSource());
//        factory.setPackagesToScan("yunrry.flik.adapters.out.persistence.vector");
//        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
//        factory.setJpaProperties(postgresProperties());
//        return factory;
//    }
//
//    @Bean(name = "vectorTransactionManager")
//    public PlatformTransactionManager vectorTransactionManager() {
//        JpaTransactionManager transactionManager = new JpaTransactionManager();
//        transactionManager.setEntityManagerFactory(vectorEntityManagerFactory().getObject());
//        return transactionManager;
//    }
//
//    private Properties postgresProperties() {
//        Properties properties = new Properties();
//        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
//        properties.setProperty("hibernate.hbm2ddl.auto", "update"); // 테스트용
//        properties.setProperty("hibernate.show_sql", "true");
//        return properties;
//    }
//}