FROM golang:1.21.0-alpine3.18
RUN mkdir /app
ADD . /app
WORKDIR /app
RUN go mod tidy
RUN go build -o main .
CMD ["/app/main"]

# docker build -t img_name
# docker run -p 8080:8080 -it img_name