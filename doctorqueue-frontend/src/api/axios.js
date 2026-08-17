import axios from "axios";

const apiClient = axios.create({
    baseURL: "http://localhost:8080/api",
    headers: {
        "Content-Type": "application/json",
    },
});

apiClient.interceptors.request.use(
    (config) => {

        const token =
            localStorage.getItem("authToken");

        if (token) {
            config.headers.Authorization =
                `Bearer ${token}`;
        }

        console.log(
            "API REQUEST:",
            config.method?.toUpperCase(),
            `${config.baseURL}${config.url}`,
            token ? "JWT PRESENT" : "NO JWT"
        );

        return config;
    },
    (error) => Promise.reject(error)
);

export default apiClient;