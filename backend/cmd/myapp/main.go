package main

import (
	"errors"
	"fmt"
	"net/http"

	"github.com/gorilla/websocket"
	"github.com/labstack/echo/v5"
	"github.com/labstack/echo/v5/middleware"
	"github.com/olekukonko/errors"
)

var upgrader = websocket.Upgrader{
	ReadBufferSize:  1024,
	WriteBufferSize: 1024,
	CheckOrigin: func(r *http.Request) bool {
		return true
	},
}

func handleWebsocket(w http.ResponseWriter, r *http.Request) error {
	conn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		return errors.New(" it we ball")
	}
	defer conn.Close()

	for {
		messType, message, err := conn.ReadMessage()
		if err != nil {
			return errors.New("Did not read the message")
			break

		}
		fmt.Println(message)
		if err := conn.WriteMessage(messType, message); err != nil {
			break
		}

	}
	return errors.New("SOS")
}

func main() {
	e := echo.New()
	e.Use(middleware.RequestLogger())

	e.Use(middleware.CORSWithConfig(middleware.CORSConfig{
		AllowOrigins: []string{"*"},
		AllowMethods: []string{
			http.MethodGet,
			http.MethodPost,
			http.MethodPut,
			http.MethodDelete,
			http.MethodOptions,
		},
	}))

	e.GET("/", func(c *echo.Context) error {
		return c.String(http.StatusOK, "Hello, World!")
	})
	if err := e.Start(":1323"); err != nil {
		e.Logger.Error("failed to start server", "error", err)
	}
}
