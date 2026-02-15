package utils

import (
	"context"
	"fmt"
	"log"
	"os"
	"time"

	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/joho/godotenv"
)

var DB *pgxpool.Pool

func InitDB() error {
	_ = godotenv.Load()

	dsn := os.Getenv("DATABASE_URL")
	if dsn == "" {
		return fatal("DATABASE_URL is missing")
	}

	cfg, err := pgxpool.ParseConfig(dsn)
	if err != nil {
		return err
	}

	cfg.MaxConns = 20
	cfg.MinConns = 5
	cfg.MaxConnLifetime = time.Hour
	cfg.MaxConnIdleTime = 30 * time.Minute

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	DB, err = pgxpool.NewWithConfig(ctx, cfg)
	if err !=
		nil {
		return err
	}

	if err = DB.Ping(ctx); err != nil {
		return err
	}
	fmt.Println("connection successful")

	return nil
}

func fatal(msg string) error {
	log.Fatal(msg)
	return nil
}
