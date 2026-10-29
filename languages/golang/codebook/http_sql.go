package codebook

import (
	"bytes"
	"context"
	"database/sql"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
	"strconv"
)

type User struct {
	ID   int64  `json:"id"`
	Name string `json:"name"`
}

// Define narrow behavior where it is consumed. A DB or fake can implement it.
type UserStore interface {
	FindUser(context.Context, int64) (User, error)
}

// UserHandler is a Go 1.21-compatible handler: GET /user?id=42. The request
// context is passed to storage; errors are mapped at the transport boundary.
func UserHandler(store UserStore) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodGet {
			w.Header().Set("Allow", http.MethodGet)
			http.Error(w, "method not allowed", http.StatusMethodNotAllowed)
			return
		}
		id, err := strconv.ParseInt(r.URL.Query().Get("id"), 10, 64)
		if err != nil || id < 1 {
			http.Error(w, "invalid id", http.StatusBadRequest)
			return
		}
		user, err := store.FindUser(r.Context(), id)
		if errors.Is(err, ErrNotFound) {
			http.Error(w, "not found", http.StatusNotFound)
			return
		}
		if err != nil {
			http.Error(w, "internal error", http.StatusInternalServerError)
			return
		}
		w.Header().Set("Content-Type", "application/json")
		if err := json.NewEncoder(w).Encode(user); err != nil {
			// After writing a response, status cannot safely be changed.
			return
		}
	})
}

// FetchUser checks status, bounds the response, and closes the body. Inject a
// configured client with a Timeout; the caller owns that client.
func FetchUser(ctx context.Context, client *http.Client, url string) (User, error) {
	request, err := http.NewRequestWithContext(ctx, http.MethodGet, url, nil)
	if err != nil {
		return User{}, err
	}
	response, err := client.Do(request)
	if err != nil {
		return User{}, err
	}
	defer response.Body.Close()
	if response.StatusCode != http.StatusOK {
		return User{}, fmt.Errorf("GET user: HTTP %d", response.StatusCode)
	}
	data, err := io.ReadAll(io.LimitReader(response.Body, (1<<20)+1))
	if err != nil {
		return User{}, err
	}
	if len(data) > 1<<20 {
		return User{}, ErrTooLarge
	}
	decoder := json.NewDecoder(bytes.NewReader(data))
	var user User
	if err := decoder.Decode(&user); err != nil {
		return User{}, err
	}
	var extra any
	if err := decoder.Decode(&extra); err != io.EOF {
		return User{}, errors.New("expected exactly one JSON value")
	}
	return user, nil
}

// SQLStore needs a registered SQL driver at the composition root. QueryRow
// returns sql.ErrNoRows for an absent record; errors.Is works on wrappers.
type SQLStore struct{ DB *sql.DB }

func (s SQLStore) FindUser(ctx context.Context, id int64) (User, error) {
	var user User
	err := s.DB.QueryRowContext(ctx, "SELECT id, name FROM users WHERE id = ?", id).Scan(&user.ID, &user.Name)
	if errors.Is(err, sql.ErrNoRows) {
		return User{}, fmt.Errorf("user %d: %w", id, ErrNotFound)
	}
	if err != nil {
		return User{}, fmt.Errorf("find user %d: %w", id, err)
	}
	return user, nil
}

// TransferMoney keeps both writes in one DB transaction. Placeholder syntax
// varies by driver (this example uses ?); each balance-changing UPDATE must
// validate rows affected and enforce nonnegative balance in its SQL predicate.
func TransferMoney(ctx context.Context, db *sql.DB, from, to int64, cents int64) error {
	if from == to || cents <= 0 {
		return errors.New("invalid transfer")
	}
	tx, err := db.BeginTx(ctx, nil)
	if err != nil {
		return err
	}
	defer tx.Rollback() // harmless after Commit; safety net on every error
	debit, err := tx.ExecContext(ctx, "UPDATE accounts SET cents = cents - ? WHERE id = ? AND cents >= ?", cents, from, cents)
	if err != nil {
		return err
	}
	n, err := debit.RowsAffected()
	if err != nil {
		return err
	}
	if n != 1 {
		return errors.New("source missing or insufficient funds")
	}
	credit, err := tx.ExecContext(ctx, "UPDATE accounts SET cents = cents + ? WHERE id = ?", cents, to)
	if err != nil {
		return err
	}
	n, err = credit.RowsAffected()
	if err != nil {
		return err
	}
	if n != 1 {
		return errors.New("destination missing")
	}
	return tx.Commit()
}
