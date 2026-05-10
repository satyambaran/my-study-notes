/*
	•	Data Flow: Prometheus collects metrics from your applications and infrastructure and stores them in its TSDB(time series data base). Grafana queries Prometheus using PromQL to visualize these metrics on customizable dashboards.
	•	Integration:
        1.	Add Prometheus as a Data Source in Grafana
        2.	Create Dashboards in Grafana: Use Grafana to create dashboards that visualize the metrics collected by Prometheus.
        3.	Set Up Alerts: Define alerts in Prometheus and Grafana to monitor critical metrics and trigger notifications when necessary.
    Use Cases:
        •	Monitoring Applications: Track application performance metrics like latency, error rates, request counts, etc.
        •	Infrastructure Monitoring: Monitor servers, containers, and network metrics to ensure system health.
        •	Capacity Planning: Use historical data to forecast resource needs and plan for scaling.
        •	Incident Response: Set up alerts for immediate notification of issues, enabling quick response and resolution.

    Getting Started:
        1.	Install Prometheus: Set up Prometheus Inside Kubernetes cluster, configure the prometheus.yml file to define the targets for scraping.
                helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
                helm repo update
                helm install prometheus prometheus-community/prometheus
                helm install grafana grafana/grafana
        2.	Install Grafana: Install Grafana, and then add Prometheus as a data source.
        3.	Create Dashboards: Query Prometheus data with PromQL.
        4.	Set Up Alerts: 
*/