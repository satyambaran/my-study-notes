package main

import (
    "crypto/sha256"
    "encoding/hex"
    "fmt"
    "hash"
)

// ServerPool represents a pool of database servers.
type ServerPool struct {
    servers []string
    hasher  hash.Hash
}

// NewServerPool creates a new ServerPool with a given list of servers.
func NewServerPool(servers []string) *ServerPool {
    return &ServerPool{
        servers: servers,
        hasher:  sha256.New(),
    }
}

// GetServer selects a server based on the key using mod hashing.
func (p *ServerPool) GetServer(key string) string {
    hashValue := p.hashKey(key)
    serverIndex := hashValue % uint32(len(p.servers))
    fmt.Println(hashValue, uint32(len(p.servers)), serverIndex)
    return p.servers[serverIndex]
}

// hashKey hashes the key using SHA-256 and returns a uint32 value.
func (p *ServerPool) hashKey(key string) uint32 {
    p.hasher.Reset()
    p.hasher.Write([]byte(key))
    hashed := p.hasher.Sum(nil)
    // fmt.Println(hashed)
    hashHex := hex.EncodeToString(hashed)
    // fmt.Println("hashhex: ", hashHex)
    var hashValue uint32
    for i := 0; i < 8; i++ {
        // fmt.Println("hashValue: ", hashValue)
        hashValue = (hashValue << 4) + uint32(hashHex[i]-'0')
    }
    fmt.Println("hashValue: ", hashValue)
    return hashValue
}

// const hexChars = "0123456789abcdef"
// for i, v := range src {
//     dst[i*2] = hexChars[v>>4]      // Extract high nibble and find corresponding hex char
//     dst[i*2+1] = hexChars[v&0x0f]   // Extract low nibble and find corresponding hex char
// }
// [253 149 128 102 121 198 20 116 190 36 99 249 0 125 60 30 36 132 105 79 138 111 154 215 174 217 77 118 41 251 44 166]
// fd   95  80  66  79  c6  14  74  be 24 63 f9 00 7d  3c 1e 24 84  69  4f 8a   6f  9a  d7 ae  d9  4d  76 29  fb 2c
// a6
// ShortenURL shortens a URL and determines which server to store it on.
func ShortenURL(pool *ServerPool, originalURL string) {
    shortened := pool.hashKey(originalURL)
    server := pool.GetServer(originalURL)
    fmt.Printf("Original URL: %s\nShortened URL: %x\nStored on: %s\n\n", originalURL, shortened, server)
}

func main() {
    servers := []string{"DBServer1", "DBServer2", "DBServer3", "DBServer4"}
    pool := NewServerPool(servers)

    urls := []string{
        "https://example.com/very/long/url/1",
        "https://example.com/another/very/long/url/2",
        "https://example.com/a/different/long/url/3",
        "https://anotherexample.com/long/url/4",
    }

    for _, url := range urls {
        ShortenURL(pool, url)
    }
}
