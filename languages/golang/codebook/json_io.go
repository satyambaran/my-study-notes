package codebook

import (
	"bytes"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"os"
)

var ErrTooLarge = errors.New("body too large")

type CreateUser struct {
	Name string `json:"name"`
	Age  int    `json:"age"`
}

// DecodeUser enforces a byte limit, rejects unknown fields/trailing values,
// and validates required business fields. encoding/json does not implement a
// "required" tag. A pointer is necessary because Decode must populate u.
func DecodeUser(src io.Reader, maxBytes int64) (CreateUser, error) {
	var u CreateUser
	if maxBytes < 0 {
		return u, fmt.Errorf("negative size limit")
	}
	data, err := io.ReadAll(io.LimitReader(src, maxBytes+1))
	if err != nil {
		return u, err
	}
	if int64(len(data)) > maxBytes {
		return u, ErrTooLarge
	}
	decoder := json.NewDecoder(bytes.NewReader(data))
	decoder.DisallowUnknownFields()
	if err := decoder.Decode(&u); err != nil {
		return CreateUser{}, fmt.Errorf("decode user: %w", err)
	}
	var extra any
	if err := decoder.Decode(&extra); err != io.EOF {
		return CreateUser{}, errors.New("expected exactly one JSON value")
	}
	if u.Name == "" || u.Age < 0 {
		return CreateUser{}, errors.New("name is required and age must be nonnegative")
	}
	return u, nil
}

// LoadUser demonstrates file ownership: open, defer close, then decode.
func LoadUser(path string, maxBytes int64) (CreateUser, error) {
	file, err := os.Open(path)
	if err != nil {
		return CreateUser{}, err
	}
	defer file.Close()
	return DecodeUser(file, maxBytes)
}
