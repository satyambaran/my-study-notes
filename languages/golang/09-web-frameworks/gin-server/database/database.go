package main

import (
    "database/sql"
    "fmt"

    "github.com/joho/godotenv"
    // "github.com/go-sql-driver/mysql"
    _ "github.com/go-sql-driver/mysql"
    // "github.com/gorilla/mux"
    // "github.com/jinzhu/gorm"
    // mysql2 "github.com/jinzhu/gorm/dialects/mysql"
)

func main() {
    err := godotenv.Load(".env")
    if err != nil {
        fmt.Println(err)
        panic(err.Error())
    }

    db, err := sql.Open("mysql", "root:Sql@123@tcp(localhost:3306)/test_db")
    if err != nil {
        fmt.Println(err)
        panic(err.Error())
    }
    defer db.Close()
    err = db.Ping()
    if err != nil {
        fmt.Println("db.Ping()", err)
        panic(err.Error())
    }
    fmt.Print("Connected to DB!!")
    query := `Create table if not exists Users{
        id int 
    }`
}

type SQLReturn struct {
    Rows  *sql.Rows
    Error error
}

func RunSQLQuery(db *sql.DB, queries []string) []SQLReturn {
    var ret []SQLReturn
    for i := 0; i < len(queries); i++ {
        rows, err := db.Query(queries[i])
        if err != nil {
            ret = append(ret, SQLReturn{nil, err})
        }
        ret = append(ret, SQLReturn{rows, err})
    }
    return ret
}
func ExecQueries(db *sql.DB, queries []string) []bool {
    ret := []bool{}
    for _, query := range queries {
        res, err := db.Exec(query)
        if err != nil {
            fmt.Println(err)
            ret = append(ret, false)
        }
        fmt.Println(res)
        ret = append(ret, true)
    }
    return ret
}

// docker run --name mysql_cont -e MYSQL_ROOT_PASSWORD=Sql@123 -d mysql:latest
// docker run --name mysql_cont -e MYSQL_ROOT_PASSWORD_FILE=/run/secrets/mysql-root -d mysql:latest
