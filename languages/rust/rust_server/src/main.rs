use actix_web::{get, post, web, App, HttpResponse, HttpServer, Responder};
use serde::{Deserialize, Serialize};

// Simple struct for JSON data
#[derive(Serialize, Deserialize)]
struct Message {
    text: String,
}

#[derive(Serialize)]
struct Response {
    status: String,
    message: String,
}

// Root endpoint
#[get("/")]
async fn index() -> impl Responder {
    HttpResponse::Ok().body("Welcome to Rust Server! 🦀\n\nAvailable endpoints:\n- GET  /\n- GET  /hello/{name}\n- POST /echo\n- GET  /api/status")
}

// Hello endpoint with path parameter
#[get("/hello/{name}")]
async fn hello(name: web::Path<String>) -> impl Responder {
    HttpResponse::Ok().body(format!("Hello, {}! 👋\n", name))
}

// Echo endpoint that accepts JSON
#[post("/echo")]
async fn echo(msg: web::Json<Message>) -> impl Responder {
    HttpResponse::Ok().json(Response {
        status: "success".to_string(),
        message: format!("You said: {}", msg.text),
    })
}

// API status endpoint
#[get("/api/status")]
async fn status() -> impl Responder {
    HttpResponse::Ok().json(Response {
        status: "ok".to_string(),
        message: "Server is running!".to_string(),
    })
}

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    let host = "127.0.0.1";
    let port = 8080;
    
    println!("🚀 Starting server at http://{}:{}", host, port);
    println!("📝 Press Ctrl+C to stop the server\n");
    
    HttpServer::new(|| {
        App::new()
            .service(index)
            .service(hello)
            .service(echo)
            .service(status)
    })
    .bind((host, port))?
    .run()
    .await
}