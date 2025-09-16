//package yunrry.flik.config;
//
//import com.zaxxer.hikari.HikariConfig;
//import com.zaxxer.hikari.HikariDataSource;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.context.annotation.Profile;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.orm.jpa.JpaTransactionManager;
//import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
//import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
//import org.springframework.transaction.PlatformTransactionManager;
//
//import jakarta.persistence.EntityManagerFactory;
//import javax.sql.DataSource;
//import java.util.Properties;
//
//@Configuration
//@Profile("!test")
//@EnableJpaRepositories(
//        basePackages = "yunrry.flik.adapters.out.persistence.mysql",
//        entityManagerFactoryRef = "mysqlEntityManagerFactory",
//        transactionManagerRef = "mysqlTransactionManager"
//)
//public class MySQLConfig {
//
//    @Value("${spring.datasource.url}")
//    private String jdbcUrl;
//
//    @Value("${spring.datasource.username}")
//    private String username;
//
//    @Value("${spring.datasource.password}")
//    private String password;
//
//    @Value("${spring.datasource.driver-class-name}")
//    private String driverClassName;
//
//    @Primary
//    @Bean(name = "mysqlDataSource")
//    public DataSource mysqlDataSource() {
//        HikariConfig config = new HikariConfig();
//        config.setJdbcUrl(jdbcUrl);
//        config.setUsername(username);
//        config.setPassword(password);
//        config.setDriverClassName(driverClassName);
//
//        // 연결 풀 설정
//        config.setMaximumPoolSize(20);
//        config.setMinimumIdle(5);
//        config.setConnectionTimeout(30000);
//        config.setIdleTimeout(600000);
//        config.setMaxLifetime(1800000);
//
//        return new HikariDataSource(config);
//    }
//
//    @Primary
//    @Bean(name = "mysqlEntityManagerFactory")
//    public LocalContainerEntityManagerFactoryBean mysqlEntityManagerFactory() {
//        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
//        factory.setDataSource(mysqlDataSource());
//        factory.setPackagesToScan("yunrry.flik.adapters.out.persistence.mysql.entity");
//        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
//        factory.setJpaProperties(hibernateProperties());
//        return factory;
//    }
//
//    @Primary
//    @Bean(name = "mysqlTransactionManager")
//    public PlatformTransactionManager mysqlTransactionManager() {
//        JpaTransactionManager transactionManager = new JpaTransactionManager();
//        transactionManager.setEntityManagerFactory(mysqlEntityManagerFactory().getObject());
//        return transactionManager;
//    }
//
//    private Properties hibernateProperties() {
//        Properties properties = new Properties();
//        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
//        properties.setProperty("hibernate.hbm2ddl.auto", "validate");
//        properties.setProperty("hibernate.show_sql", "false");
//        properties.setProperty("hibernate.format_sql", "true");
//        properties.setProperty("hibernate.use_sql_comments", "true");
//        return properties;
//    }
//}