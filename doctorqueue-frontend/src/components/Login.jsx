import { useState } from "react";
import apiClient from "../api/axios";

function Login({
                   onLogin,
                   onSwitchToRegister
               }) {

    const [email, setEmail] =
        useState("");

    const [password, setPassword] =
        useState("");

    const [error, setError] =
        useState("");

    const [loading, setLoading] =
        useState(false);

    const handleSubmit = async event => {

        event.preventDefault();

        setError("");
        setLoading(true);

        try {

            const response =
                await apiClient.post(
                    "/auth/login",
                    {
                        email,
                        password
                    }
                );

            const user =
                response.data;

            if (!user.token) {
                throw new Error(
                    "Login token was not returned."
                );
            }

            // Save JWT
            localStorage.setItem(
                "authToken",
                user.token
            );

            // Save complete user
            localStorage.setItem(
                "authUser",
                JSON.stringify(user)
            );

            // Tell App
            onLogin(user);

        } catch (error) {

            console.error(
                "Login error:",
                error
            );

            setError(
                error.response?.data?.message ||
                error.response?.data?.error ||
                "Invalid email or password."
            );

        } finally {

            setLoading(false);
        }
    };

    return (
        <div className="auth-container">

            <div className="auth-card">

                <h1>
                    Doctor Queue
                </h1>

                <h2>
                    Login
                </h2>

                <form
                    onSubmit={handleSubmit}
                >

                    <input
                        type="email"
                        placeholder="Email"
                        value={email}
                        onChange={event =>
                            setEmail(
                                event.target.value
                            )
                        }
                        required
                    />

                    <input
                        type="password"
                        placeholder="Password"
                        value={password}
                        onChange={event =>
                            setPassword(
                                event.target.value
                            )
                        }
                        required
                    />

                    {error && (
                        <div className="message">
                            {error}
                        </div>
                    )}

                    <button
                        type="submit"
                        disabled={loading}
                    >
                        {loading
                            ? "Logging in..."
                            : "LOGIN"}
                    </button>

                </form>

                <p>
                    Don't have an account?
                </p>

                <button
                    type="button"
                    onClick={
                        onSwitchToRegister
                    }
                >
                    REGISTER
                </button>

            </div>

        </div>
    );
}

export default Login;