package com.example.demo.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import javax.sql.DataSource;

@Configuration
@MapperScan(value = "com.example.demo.source", sqlSessionFactoryRef ="sourceSqlSessionFactory")
public class SourceDatabaseConfig {
    @Autowired
    private ApplicationContext applicationContext;

    @Bean("sourceDatasourceProperties")
    @ConfigurationProperties("demo.datasource.source")
    public DataSourceProperties dataSourceProperties(){
        return new DataSourceProperties();
    }

    @Bean("sourceDataSource")
    @ConfigurationProperties("demo.datasource.source")
    public DataSource dataSource() {
        return dataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean("sourceSqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(
            @Qualifier("sourceDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        return sqlSessionFactoryBean.getObject();
    }

    @Bean("sourceSqlSession")
    public SqlSessionTemplate sqlSessionCommonTemplate(@Qualifier("sourceSqlSessionFactory") SqlSessionFactory sqlSessionFactory){
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean("sourceDatasourceTransactionManager")
    public DataSourceTransactionManager dataSourceTransactionManager(@Qualifier("sourceDataSource")
                                                                     DataSource dataSource){
        return new DataSourceTransactionManager(dataSource);

    }

}
