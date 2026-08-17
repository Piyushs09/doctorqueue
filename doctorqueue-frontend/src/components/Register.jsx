import { useEffect, useState } from "react";
import apiClient from "../api/axios";

function Register({
                      onRegistered,
                      onSwitchToLogin
                  }) {

    const [accountType, setAccountType] =
        useState("PATIENT");

    const [clinics, setClinics] =
        useState([]);

    const [clinicId, setClinicId] =
        useState("");

    const [specialization, setSpecialization] =
        useState("");

    const [name, setName] =
        useState("");

    const [email, setEmail] =
        useState("");

    const [phone, setPhone] =
        useState("");

    const [password, setPassword] =
        useState("");

    const [error, setError] =
        useState("");

    const [loading, setLoading] =
        useState(false);

    const isDoctor =
        accountType === "DOCTOR";


    // LOAD CLINICS WHEN DOCTOR IS SELECTED
    useEffect(() => {

        if (!isDoctor) {
            setClinics([]);
            setClinicId("");
            return;
        }

        const loadClinics = async () => {

            try {

                console.log("Loading clinics...");

                const response =
                    await apiClient.get("/clinics");

                console.log(
                    "Clinic API response:",
                    response
                );

                console.log(
                    "Clinic data:",
                    response.data
                );

                if (Array.isArray(response.data)) {

                    setClinics(response.data);

                } else {

                    console.error(
                        "Expected clinic array:",
                        response.data
                    );

                    setClinics([]);
                }

            } catch (error) {

                console.error(
                    "Unable to load clinics:",
                    error
                );

                console.error(
                    "Status:",
                    error.response?.status
                );

                console.error(
                    "Response:",
                    error.response?.data
                );

                setClinics([]);
            }
        };

        loadClinics();

    }, [isDoctor]);


    const handleSubmit = async event => {

        event.preventDefault();

        setError("");
        setLoading(true);

        try {

            const request = {
                name: name.trim(),
                email: email.trim(),
                phone: phone.trim(),
                password,
                role: accountType
            };

            if (isDoctor) {

                if (!specialization) {
                    setError(
                        "Please select specialization."
                    );
                    setLoading(false);
                    return;
                }

                if (!clinicId) {
                    setError(
                        "Please select a clinic."
                    );
                    setLoading(false);
                    return;
                }

                request.specialization =
                    specialization;

                request.clinicId =
                    Number(clinicId);
            }

            const response =
                await apiClient.post(
                    "/auth/register",
                    request
                );

            const user =
                response.data;

            if (!user.token) {
                throw new Error(
                    "Registration token was not returned."
                );
            }

            localStorage.setItem(
                "authToken",
                user.token
            );

            localStorage.setItem(
                "authUser",
                JSON.stringify(user)
            );

            onRegistered(user);

        } catch (error) {

            console.error(
                "Registration error:",
                error
            );

            setError(
                error.response?.data?.message ||
                error.response?.data?.error ||
                error.message ||
                "Registration failed."
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
                    Create Account
                </h2>

                <form onSubmit={handleSubmit}>

                    <label>
                        Account Type
                    </label>

                    <div className="account-type">

                        <label>
                            <input
                                type="radio"
                                name="accountType"
                                value="PATIENT"
                                checked={
                                    accountType ===
                                    "PATIENT"
                                }
                                onChange={() => {
                                    setAccountType(
                                        "PATIENT"
                                    );
                                    setClinicId("");
                                    setSpecialization("");
                                }}
                            />

                            Patient
                        </label>

                        <label>
                            <input
                                type="radio"
                                name="accountType"
                                value="DOCTOR"
                                checked={
                                    accountType ===
                                    "DOCTOR"
                                }
                                onChange={() => {
                                    setAccountType(
                                        "DOCTOR"
                                    );
                                }}
                            />

                            Doctor
                        </label>

                    </div>

                    <input
                        type="text"
                        placeholder="Full name"
                        value={name}
                        onChange={e =>
                            setName(e.target.value)
                        }
                        required
                    />

                    <input
                        type="email"
                        placeholder="Email"
                        value={email}
                        onChange={e =>
                            setEmail(e.target.value)
                        }
                        required
                    />

                    <input
                        type="text"
                        placeholder="Phone"
                        value={phone}
                        onChange={e =>
                            setPhone(e.target.value)
                        }
                    />

                    {isDoctor && (

                        <>
                            <label>
                                Specialization
                            </label>

                            <select
                                value={specialization}
                                onChange={e =>
                                    setSpecialization(
                                        e.target.value
                                    )
                                }
                            >
                                <option value="">
                                    Select specialization
                                </option>

                                <option value="General Physician">
                                    General Physician
                                </option>

                                <option value="Cardiologist">
                                    Cardiologist
                                </option>

                                <option value="Dermatologist">
                                    Dermatologist
                                </option>

                                <option value="Dentist">
                                    Dentist
                                </option>

                                <option value="Orthopedic">
                                    Orthopedic
                                </option>

                                <option value="Pediatrician">
                                    Pediatrician
                                </option>

                                <option value="Gynecologist">
                                    Gynecologist
                                </option>
                            </select>

                            <label>
                                Select Clinic
                            </label>

                            <select
                                value={clinicId}
                                onChange={e =>
                                    setClinicId(e.target.value)
                                }
                                required
                            >
                                <option value="">
                                    {clinics.length === 0
                                        ? "No clinics available"
                                        : "Select clinic"}
                                </option>

                                {clinics.map(clinic => (
                                    <option
                                        key={clinic.id}
                                        value={clinic.id}
                                    >
                                        {clinic.name}
                                    </option>
                                ))}
                            </select>

                        </>
                    )}

                    <input
                        type="password"
                        placeholder="Password"
                        value={password}
                        onChange={e =>
                            setPassword(
                                e.target.value
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
                            ? "Creating..."
                            : "CREATE ACCOUNT"}
                    </button>

                </form>

                <p>
                    Already have an account?
                </p>

                <button
                    type="button"
                    onClick={
                        onSwitchToLogin
                    }
                >
                    LOGIN
                </button>

            </div>

        </div>
    );
}

export default Register;