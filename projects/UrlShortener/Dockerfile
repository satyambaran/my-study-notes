# ---------- build stage ----------
FROM golang:1.21-alpine AS builder

WORKDIR /app

COPY go.mod go.sum ./
RUN go mod download

COPY . .
RUN CGO_ENABLED=0 GOOS=linux go build -o server .

# ---------- runtime stage ----------
FROM alpine:3.19

# ca-certificates lets the app make outbound HTTPS calls if needed
RUN apk --no-cache add ca-certificates

WORKDIR /app
COPY --from=builder /app/server .

EXPOSE 3000
CMD ["./server"]
