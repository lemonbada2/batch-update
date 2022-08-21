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
@MapperScan(value = "com.example.demo.target", sqlSessionFactoryRef = "targetSqlSessionFactory")
public class TargetDatabaseConfig {
    @Autowired
    private ApplicationContext applicationContext;

    @Bean("targetDatasourceProperties")
    @ConfigurationProperties("demo.datasource.target")
    public DataSourceProperties dataSourceProperties(){
        return new DataSourceProperties();
    }

    @Bean("targetDataSource")
    @ConfigurationProperties("demo.datasource.target")
    public DataSource dataSource() {
        return dataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean("targetSqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(
            @Qualifier("targetDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        return sqlSessionFactoryBean.getObject();
    }

    @Bean("targetSqlSession")
    public SqlSessionTemplate sqlSessionCommonTemplate(@Qualifier("targetSqlSessionFactory") SqlSessionFactory sqlSessionFactory){
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean("targetDatasourceTransactionManager")
    public DataSourceTransactionManager dataSourceTransactionManager(@Qualifier("targetDataSource")
                                                                     DataSource dataSource){
        return new DataSourceTransactionManager(dataSource);

    }

}
