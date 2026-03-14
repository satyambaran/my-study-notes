.PHONY: test unit integration

# Run all Go unit tests
unit:
	go test ./... -v

# Run end-to-end integration tests (server must be running first)
# Usage: make integration BASE=http://192.168.x.x:30300
BASE ?= http://localhost:3000
integration:
	./tests/integration.sh $(BASE)

# Run unit tests then integration tests
test: unit integration
