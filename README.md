# High-Performance jOOQ

The High-Performance jOOQ repository provides code examples that demonstrate how jOOQ works.

### Are you struggling with application performance issues?

<a href="https://vladmihalcea.com/hypersistence-optimizer/?utm_source=GitHub&utm_medium=banner&utm_campaign=hpjp">
<img src="https://vladmihalcea.com/wp-content/uploads/2019/03/Hypersistence-Optimizer-300x250.jpg" alt="Hypersistence Optimizer">
</a>

Imagine having a tool that can automatically detect if you are using JPA and Hibernate properly. No more performance issues, no more having to spend countless hours trying to figure out why your application is barely crawling.

Imagine discovering early during the development cycle that you are using suboptimal mappings and entity relationships or that you are missing performance-related settings. 

More, with Hypersistence Optimizer, you can detect all such issues during testing and make sure you don't deploy to production a change that will affect data access layer performance.

[Hypersistence Optimizer](https://vladmihalcea.com/hypersistence-optimizer/?utm_source=GitHub&utm_medium=banner&utm_campaign=hpjp) is the tool you've been long waiting for!

#### Training

If you are interested in on-site training, I can offer you my [High-Performance Java Persistence training](https://vladmihalcea.com/trainings/?utm_source=GitHub&utm_medium=banner&utm_campaign=hpjp)
which can be adapted to one, two or three days of sessions. For more details, check out [my website](https://vladmihalcea.com/trainings/?utm_source=GitHub&utm_medium=banner&utm_campaign=hpjp).

#### Consulting

If you want me to review your application and provide insight into how you can optimize it to run faster, 
then check out my [consulting page](https://vladmihalcea.com/consulting/?utm_source=GitHub&utm_medium=banner&utm_campaign=hpjp).

#### High-Performance Java Persistence Video Courses

If you want the fastest way to learn how to speed up a Java database application, then you should definitely enroll in [my High-Performance Java Persistence video courses](https://vladmihalcea.com/courses/?utm_source=GitHub&utm_medium=banner&utm_campaign=hpjp).

#### High-Performance Java Persistence Book

Or, if you prefer reading books, you are going to love my [High-Performance Java Persistence book](https://vladmihalcea.com/books/high-performance-java-persistence?utm_source=GitHub&utm_medium=banner&utm_campaign=hpjp) as well.

<a href="https://vladmihalcea.com/books/high-performance-java-persistence?utm_source=GitHub&utm_medium=banner&utm_campaign=hpjp">
<img src="https://i0.wp.com/vladmihalcea.com/wp-content/uploads/2018/01/HPJP_h200.jpg" alt="High-Performance Java Persistence book">
</a>

<a href="https://vladmihalcea.com/courses?utm_source=GitHub&utm_medium=banner&utm_campaign=hpjp">
<img src="https://i0.wp.com/vladmihalcea.com/wp-content/uploads/2018/01/HPJP_Video_Vertical_h200.jpg" alt="High-Performance Java Persistence video course">
</a>

## Java

All examples require at least Java 17 because of the awesome [Text Blocks](https://openjdk.java.net/jeps/355) feature, which makes JPQL and SQL queries so much readable.

## Maven

You need to use Maven 3.6.2 or newer to build the project.

## IntelliJ IDEA

On IntelliJ IDEA, the project runs just fine. You will have to make sure to select Java 17 or newer.

## Database setup

The project uses various database systems for integration testing, and you can configure the JDBC connection settings using the
`DatasourceProvider` instances (e.g., `PostgreSQLDataSourceProvider`).

### Manual Database configuration

- PostgreSQL

    You can install PostgreSQL, and the password for the `postgres` user should be `admin`.

    Now you need to create a `high_performance_java_persistence` database.
    
- Oracle

    You need to download and install Oracle XE

    Set the `sys` password to `admin`

    Connect to Oracle using the "sys as sysdba" user and create a new user:
    
        alter session set "_ORACLE_SCRIPT"=true;

        create user oracle identified by admin default tablespace users;

        grant dba to oracle;

        alter system set processes=1000 scope=spfile;

        alter system set sessions=1000 scope=spfile;
        
        ALTER PROFILE DEFAULT LIMIT PASSWORD_LIFE_TIME UNLIMITED;

    Open the `C:\app\${user.name}\product\21c\homes\OraDB21Home1\network\admin` folder where `${user.name}` is your current Windows username.
  
    Locate the `tnsnames.ora` and `listener.ora` files and change the port from `1522` to `1521` if that's the case. If you made these modifications,
    you need to restart the `OracleOraDB21Home1TNSListener` and `OracleServiceXE` Windows services.
  
- MySQL

    You should install MySQL 8, and the password for the `mysql` user should be `admin`.

    Now, you need to create a `high_performance_java_persistence` schema

    Besides having all privileges on this schema, the `mysql` user also requires select permission on `mysql.PROC`.
    
    If you don't have a `mysql` user created at database installation time, you can create one as follows:
    
    ````
    CREATE USER 'mysql'@'localhost';
    
    SET PASSWORD for 'mysql'@'localhost'='admin';
    
    GRANT ALL PRIVILEGES ON high_performance_java_persistence.* TO 'mysql'@'localhost';
    
    GRANT SELECT ON mysql.* TO 'mysql'@'localhost';
    
    FLUSH PRIVILEGES;
    ````

- SQL Server

    You can install SQL Server Express Edition with Tools. Choose mixed mode authentication and set the `sa` user password to `adm1n`.

    Open SQL Server Configuration Manager -> SQL Server Network Configuration and enable Named Pipes and TCP
    
    In the right pane of the TCP/IP option, choose Properties, then IP Addresses and make sure you Enable all listed IP addresses.
    You need to blank the dynamic TCP port value and configure the static TCP port 1433 for all IPs.
        
    Open SQL Server Management Studio and create the `high_performance_java_persistence` database
    
## Maven

> To build the project, don't use *install* or *package*. Instead, just compile test classes like this:
>
>    mvn clean test-compile
    
Afterward, just pick one test from the IDE and run it individually.

> Don't you run all tests at once (e.g. `mvn clean test`) because the test suite will take a very long time to complete.
>
> So, run the test you are interested in individually.

Enjoy learning more about jOOQ and database systems!
