import Login from "./components/Login";
import Register from "./components/Register";
import { useEffect, useState, useCallback } from "react";
import apiClient from "./api/axios";
import "./App.css";

const API = "/queue";
const DOCTOR_API = "/doctors";
const EVENTS_API = "/queue/events";
const PATIENT_QUEUE_STORAGE = "doctorQueueId";
const PATIENT_DOCTOR_STORAGE = "patientDoctorId";

function App() {

    // =========================================================
    // AUTH USER
    // =========================================================

    const [authUser, setAuthUser] = useState(() => {

        const savedUser =
            localStorage.getItem("authUser");

        if (!savedUser) {
            return null;
        }

        try {
            return JSON.parse(savedUser);
        } catch {
            localStorage.removeItem("authUser");
            localStorage.removeItem("authToken");
            return null;
        }
    });

    const [authScreen, setAuthScreen] =
        useState("login");

    // =========================================================
    // MODE
    // =========================================================

    const [mode, setMode] =
        useState("patient");

    // =========================================================
    // DOCTORS
    // =========================================================

    const [doctors, setDoctors] =
        useState([]);

    const [selectedDoctorId, setSelectedDoctorId] =
        useState("");

    // =========================================================
    // PATIENT
    // =========================================================

    const [queueId, setQueueId] =
        useState(null);

    const [patientName, setPatientName] =
        useState("");

    const [patientDashboard, setPatientDashboard] =
        useState(null);

    const [joiningQueue, setJoiningQueue] =
        useState(false);

    // =========================================================
    // DOCTOR
    // =========================================================

    const [doctorDashboard, setDoctorDashboard] =
        useState(null);

    const [doctorQueue, setDoctorQueue] =
        useState([]);

    const [completedToday, setCompletedToday] =
        useState([]);

    const [doctorStatistics, setDoctorStatistics] =
        useState(null);

    // =========================================================
    // COMMON
    // =========================================================

    const [loading, setLoading] =
        useState(false);

    const [message, setMessage] =
        useState("");

    // =========================================================
    // LOGOUT
    // =========================================================

    const logout = () => {

        localStorage.removeItem("authToken");
        localStorage.removeItem("authUser");


        setAuthUser(null);
        setQueueId(null);
        setPatientDashboard(null);
        setDoctorDashboard(null);
        setDoctorQueue([]);
        setCompletedToday([]);
        setMessage("");
        setMode("patient");
    };

    // =========================================================
    // SET INITIAL MODE FROM ROLE
    // =========================================================

    useEffect(() => {

        if (!authUser) {
            return;
        }

        const role =
            String(authUser.role || "").toUpperCase();

        if (role === "DOCTOR") {
            setMode("doctor");
        } else {
            setMode("patient");
        }

    }, [authUser]);

    // =========================================================
    // LOAD DOCTORS


    // PATIENT ONLY
    // =========================================================

    const loadDoctors = useCallback(async () => {

        if (!authUser) {
            return;
        }

        if (
            String(authUser.role).toUpperCase() !==
            "PATIENT"
        ) {
            return;
        }

        try {

            const response =
                await apiClient.get(DOCTOR_API);

            const doctorList =
                Array.isArray(response.data)
                    ? response.data
                    : [];

            setDoctors(doctorList);

            if (doctorList.length === 0) {
                setSelectedDoctorId("");
                return;
            }

            const savedDoctor =
                localStorage.getItem(
                    PATIENT_DOCTOR_STORAGE
                );

            const exists =
                savedDoctor &&
                doctorList.some(
                    doctor =>
                        String(doctor.id) ===
                        String(savedDoctor)
                );

            if (exists) {

                setSelectedDoctorId(
                    String(savedDoctor)
                );

            } else {

                setSelectedDoctorId(
                    String(doctorList[0].id)
                );
            }

        } catch (error) {

            console.error(
                "Unable to load doctors:",
                error
            );

            setMessage(
                error.response?.data?.message ||
                "Unable to load doctors."
            );
        }

    }, [authUser]);

    useEffect(() => {

        loadDoctors();

    }, [loadDoctors]);

    // =========================================================
    // PATIENT DASHBOARD
    // =========================================================

    const loadPatientDashboard =
        useCallback(async (id) => {

            if (!id) {
                return;
            }

            try {

                const response =
                    await apiClient.get(
                        `/queue/patient/${id}/dashboard`
                    );

                setPatientDashboard(
                    response.data
                );

            } catch (error) {

                console.error(
                    "Patient dashboard error:",
                    error
                );

                if (
                    error.response?.status === 404
                ) {

                    setQueueId(null);
                    setPatientDashboard(null);

                    localStorage.removeItem(
                        PATIENT_QUEUE_STORAGE
                    );
                }

                if (
                    error.response?.status === 401 ||
                    error.response?.status === 403
                ) {

                    logout();
                }
            }

        }, []);

// =========================================================
// RESTORE PATIENT QUEUE AFTER LOGIN / PAGE REFRESH
// =========================================================

    useEffect(() => {

        if (!authUser) {
            return;
        }

        if (
            String(authUser.role || "").toUpperCase() !==
            "PATIENT"
        ) {
            return;
        }

        const restorePatientQueue = async () => {

            try {

                console.log(
                    "Restoring current patient queue..."
                );

                const response =
                    await apiClient.get(
                        "/queue/patient/current"
                    );

                console.log(
                    "CURRENT PATIENT QUEUE:",
                    response.data
                );

                const dashboard =
                    response.data;

                if (!dashboard || !dashboard.queueId) {

                    console.log(
                        "Patient has no active queue."
                    );

                    setQueueId(null);
                    setPatientDashboard(null);

                    localStorage.removeItem(
                        PATIENT_QUEUE_STORAGE
                    );

                    return;
                }

                const restoredQueueId =
                    Number(dashboard.queueId);

                setQueueId(
                    restoredQueueId
                );

                setPatientDashboard(
                    dashboard
                );

                if (dashboard.doctorId) {

                    setSelectedDoctorId(
                        String(dashboard.doctorId)
                    );

                    localStorage.setItem(
                        PATIENT_DOCTOR_STORAGE,
                        String(dashboard.doctorId)
                    );
                }

                localStorage.setItem(
                    PATIENT_QUEUE_STORAGE,
                    String(restoredQueueId)
                );

                setMessage("");

            } catch (error) {

                console.error(
                    "RESTORE PATIENT QUEUE ERROR:",
                    error
                );

                console.error(
                    "STATUS:",
                    error.response?.status
                );

                console.error(
                    "URL:",
                    error.config?.url
                );

                console.error(
                    "RESPONSE:",
                    error.response?.data
                );

                // Only clear the queue for 404.
                // Do NOT destroy the queue state on 403/500.
                if (
                    error.response?.status === 404
                ) {

                    setQueueId(null);
                    setPatientDashboard(null);

                    localStorage.removeItem(
                        PATIENT_QUEUE_STORAGE
                    );
                }
            }
        };

        restorePatientQueue();

    }, [authUser]);

    // =========================================================
    // DOCTOR ID
    // =========================================================

    const getLoggedInDoctorId = () => {

        const storedUser =
            localStorage.getItem("authUser");

        if (!storedUser) {
            return null;
        }

        try {

            const user =
                JSON.parse(storedUser);

            console.log("AUTH USER:", user);

            if (
                user.role?.toUpperCase() !== "DOCTOR"
            ) {
                return null;
            }

            return user.doctorId
                ? Number(user.doctorId)
                : null;

        } catch (error) {

            console.error(
                "Invalid authUser:",
                error
            );

            return null;
        }
    };

    // =========================================================
    // LOAD DOCTOR DASHBOARD
    // =========================================================

    const loadDoctorData = useCallback(async () => {

        const doctorId =
            getLoggedInDoctorId();

        console.log(
            "LOADING DOCTOR ID:",
            doctorId
        );

        if (!doctorId) {
            setDoctorDashboard(null);
            setMessage(
                "Doctor profile is not linked to this account."
            );
            return;
        }

        try {

            const [
                dashboardResponse,
                queueResponse,
                completedResponse,
                statisticsResponse
            ] = await Promise.all([

                apiClient.get(
                    `/queue/doctor/${doctorId}/dashboard`
                ),

                apiClient.get(
                    `/queue/doctor/${doctorId}`
                ),

                apiClient.get(
                    `/queue/doctor/${doctorId}/completed-today`
                ),

                apiClient.get(
                    `/queue/doctor/${doctorId}/statistics`
                )

            ]);

            console.log(
                "DASHBOARD RESPONSE:",
                dashboardResponse.data
            );

            setDoctorDashboard(
                dashboardResponse.data
            );

            setDoctorQueue(
                Array.isArray(queueResponse.data)
                    ? queueResponse.data
                    : []
            );

            setCompletedToday(
                Array.isArray(completedResponse.data)
                    ? completedResponse.data
                    : []
            );

            setDoctorStatistics(
                statisticsResponse.data
            );

            setMessage("");

        } catch (error) {

            console.error(
                "DOCTOR DASHBOARD ERROR:",
                error
            );

            console.error(
                "STATUS:",
                error.response?.status
            );

            console.error(
                "DATA:",
                error.response?.data
            );

            setMessage(
                error.response?.data?.message ||
                error.response?.data?.error ||
                "Unable to load doctor dashboard."
            );
        }

    }, []);


// =========================================================
// LOAD DOCTOR DASHBOARD WHEN DOCTOR LOGS IN
// =========================================================

    useEffect(() => {

        if (!authUser) {
            return;
        }

        const role =
            String(authUser.role || "").toUpperCase();

        if (role !== "DOCTOR") {
            return;
        }

        console.log(
            "Doctor logged in. Loading dashboard..."
        );

        loadDoctorData();

    }, [
        authUser,
        loadDoctorData
    ]);

    // =========================================================
    // SSE
    // =========================================================

    useEffect(() => {

        if (!authUser) {
            return;
        }

        const eventSource =
            new EventSource(EVENTS_API);

        eventSource.addEventListener(
            "connected",
            event => {

                console.log(
                    "SSE connected:",
                    event.data
                );
            }
        );

        eventSource.addEventListener(
            "queue-updated",
            () => {

                if (
                    String(authUser.role).toUpperCase() ===
                    "PATIENT"
                ) {

                    if (queueId) {
                        loadPatientDashboard(
                            queueId
                        );
                    }

                } else if (
                    String(authUser.role).toUpperCase() ===
                    "DOCTOR"
                ) {

                    loadDoctorData();
                }
            }
        );

        eventSource.onerror = () => {

            console.log(
                "SSE connection lost. Browser will retry..."
            );
        };

        return () => {

            eventSource.close();

        };

    }, [
        authUser,
        queueId,
        loadPatientDashboard,
        loadDoctorData
    ]);

    // =========================================================
    // JOIN QUEUE
    // =========================================================

    const joinQueue = async event => {

        event.preventDefault();

        if (
            String(authUser?.role).toUpperCase() !==
            "PATIENT"
        ) {

            setMessage(
                "Only patients can join a queue."
            );

            return;
        }

        if (!selectedDoctorId) {

            setMessage(
                "Please select a doctor."
            );

            return;
        }

        if (!patientName.trim()) {

            setMessage(
                "Please enter your name."
            );

            return;
        }

        setJoiningQueue(true);
        setMessage("");

        try {

            const response = await apiClient.post(
                "/queue/join",
                {
                    doctorId: Number(selectedDoctorId),
                    patientName: patientName.trim()
                }
            );

            console.log("JOIN RESPONSE:", response.data);

            const newQueueId = response.data.queueId;

            if (!newQueueId) {
                throw new Error(
                    "Server did not return queueId."
                );
            }

            console.log(
                "SETTING QUEUE ID:",
                newQueueId
            );

            setQueueId(Number(newQueueId));

            localStorage.setItem(
                "doctorQueueId",
                String(newQueueId)
            );

            localStorage.setItem(
                "patientDoctorId",
                String(selectedDoctorId)
            );

// Directly load this exact queue
            const dashboardResponse =
                await apiClient.get(
                    `/queue/patient/${newQueueId}/dashboard`
                );

            console.log(
                "PATIENT DASHBOARD:",
                dashboardResponse.data
            );

            setPatientDashboard(
                dashboardResponse.data
            );

            setMessage(
                `Successfully joined queue. Your token is #${response.data.tokenNumber}`
            );

            await loadPatientDashboard(
                Number(newQueueId)
            );

        } catch (error) {

            console.error(
                "Join queue error:",
                error
            );

            setMessage(
                error.response?.data?.message ||
                error.response?.data?.error ||
                error.message ||
                "Unable to join queue."
            );

        } finally {

            setJoiningQueue(false);
        }
    };

    // =========================================================
    // CANCEL PATIENT QUEUE
    // =========================================================

    const cancelMyQueue = async () => {

        if (!queueId) {
            return;
        }

        const confirmed =
            window.confirm(
                "Cancel your queue?"
            );

        if (!confirmed) {
            return;
        }

        try {

            await apiClient.delete(
                `${API}/${queueId}`
            );

            setMessage(
                "Your queue has been cancelled."
            );

            setQueueId(null);
            setPatientDashboard(null);

            localStorage.removeItem(
                PATIENT_QUEUE_STORAGE
            );

        } catch (error) {

            console.error(
                "Cancel queue error:",
                error
            );

            setMessage(
                error.response?.data?.message ||
                "Unable to cancel queue."
            );
        }
    };

    // =========================================================
    // RESET PATIENT
    // =========================================================

    const resetPatient = () => {

        setQueueId(null);
        setPatientDashboard(null);
        setPatientName("");
        setMessage("");

        localStorage.removeItem(
            PATIENT_QUEUE_STORAGE
        );
    };

    // =========================================================
    // DOCTOR: START NEXT
    // =========================================================

    const startNextPatient = async () => {

        const doctorId =
            getLoggedInDoctorId();

        if (!doctorId) {
            setMessage(
                "Doctor account is not linked."
            );
            return;
        }

        setLoading(true);
        setMessage("");

        try {

            await apiClient.post(
                `${API}/doctor/${doctorId}/next`
            );

            setMessage(
                "Next patient started."
            );

            await loadDoctorData();

        } catch (error) {

            console.error(
                "Start next patient error:",
                error
            );

            setMessage(
                error.response?.data?.message ||
                "Unable to start next patient."
            );

        } finally {

            setLoading(false);
        }
    };

    // =========================================================
    // DOCTOR: COMPLETE
    // =========================================================

    const completePatient = async () => {

        const doctorId =
            getLoggedInDoctorId();

        if (!doctorId) {
            return;
        }

        setLoading(true);
        setMessage("");

        try {

            await apiClient.post(
                `${API}/doctor/${doctorId}/complete`
            );

            setMessage(
                "Patient completed."
            );

            await loadDoctorData();

        } catch (error) {

            console.error(
                "Complete patient error:",
                error
            );

            setMessage(
                error.response?.data?.message ||
                "Unable to complete patient."
            );

        } finally {

            setLoading(false);
        }
    };

    // =========================================================
    // DOCTOR: SKIP
    // =========================================================

    const skipPatient = async () => {

        const doctorId =
            getLoggedInDoctorId();

        if (!doctorId) {
            return;
        }

        const confirmed =
            window.confirm(
                "Are you sure you want to skip this patient?"
            );

        if (!confirmed) {
            return;
        }

        setLoading(true);
        setMessage("");

        try {

            await apiClient.post(
                `${API}/doctor/${doctorId}/skip`
            );

            setMessage(
                "Patient skipped."
            );

            await loadDoctorData();

        } catch (error) {

            console.error(
                "Skip patient error:",
                error
            );

            setMessage(
                error.response?.data?.message ||
                "Unable to skip patient."
            );

        } finally {

            setLoading(false);
        }
    };

    // =========================================================
    // DOCTOR: CANCEL QUEUE
    // =========================================================

    const cancelPatient = async id => {

        if (!id) {
            return;
        }

        const confirmed =
            window.confirm(
                "Are you sure you want to cancel this queue?"
            );

        if (!confirmed) {
            return;
        }

        try {

            await apiClient.delete(
                `${API}/${id}`
            );

            setMessage(
                "Queue cancelled."
            );

            await loadDoctorData();

        } catch (error) {

            console.error(
                "Cancel queue error:",
                error
            );

            setMessage(
                error.response?.data?.message ||
                "Unable to cancel queue."
            );
        }
    };

    // =========================================================
    // DOCTOR: COMPLETE SKIPPED
    // =========================================================

    const completeSkippedPatient =
        async queueIdToComplete => {

            const doctorId =
                getLoggedInDoctorId();

            if (!queueIdToComplete || !doctorId) {
                return;
            }

            const confirmed =
                window.confirm(
                    "Mark this skipped patient as completed?"
                );

            if (!confirmed) {
                return;
            }

            setLoading(true);
            setMessage("");

            try {

                await apiClient.post(
                    `${API}/doctor/${doctorId}/skipped/${queueIdToComplete}/complete`
                );

                setMessage(
                    "Skipped patient marked as completed."
                );

                await loadDoctorData();

            } catch (error) {

                console.error(
                    "Complete skipped patient error:",
                    error
                );

                setMessage(
                    error.response?.data?.message ||
                    "Unable to complete skipped patient."
                );

            } finally {

                setLoading(false);
            }
        };

    // =========================================================
    // PATIENT JOIN SCREEN
    // =========================================================

    const renderJoinQueue = () => {

        const selectedDoctor =
            doctors.find(
                doctor =>
                    String(doctor.id) ===
                    String(selectedDoctorId)
            );

        return (
            <div className="dashboard">

                <h1>
                    Join Doctor Queue
                </h1>

                <form
                    className="join-form"
                    onSubmit={joinQueue}
                >

                    <label>
                        Select Doctor
                    </label>

                    <select
                        value={selectedDoctorId}
                        onChange={event => {

                            const value =
                                event.target.value;

                            setSelectedDoctorId(
                                value
                            );

                            localStorage.setItem(
                                PATIENT_DOCTOR_STORAGE,
                                value
                            );

                        }}
                    >

                        <option value="">
                            Select a doctor
                        </option>

                        {doctors.map(
                            doctor => (
                                <option
                                    key={doctor.id}
                                    value={doctor.id}
                                >
                                    {doctor.name}
                                    {doctor.specialization
                                        ? ` - ${doctor.specialization}`
                                        : ""}
                                </option>
                            )
                        )}

                    </select>

                    {selectedDoctor && (

                        <div className="selected-doctor">

                            <strong>
                                {selectedDoctor.name}
                            </strong>

                            <span>
                                {
                                    selectedDoctor.specialization ||
                                    "General Physician"
                                }
                            </span>

                            <span>
                                {
                                    selectedDoctor.clinic?.name ||
                                    "Clinic"
                                }
                            </span>

                        </div>
                    )}

                    <label>
                        Patient Name
                    </label>

                    <input
                        type="text"
                        placeholder="Enter your name"
                        value={patientName}
                        onChange={event =>
                            setPatientName(
                                event.target.value
                            )
                        }
                    />

                    <button
                        className="next-button"
                        type="submit"
                        disabled={
                            joiningQueue ||
                            !selectedDoctorId ||
                            doctors.length === 0
                        }
                    >
                        {joiningQueue
                            ? "Joining..."
                            : "JOIN QUEUE"}
                    </button>

                </form>

                {message && (
                    <div className="message">
                        {message}
                    </div>
                )}

            </div>
        );
    };

    // =========================================================
    // PATIENT DASHBOARD
    // =========================================================

    const renderPatientDashboard = () => {

        if (!patientDashboard) {

            return (
                <div className="loading">
                    Loading patient dashboard...
                </div>
            );
        }

        return (
            <div className="dashboard">

                <h1>
                    Your Queue Status
                </h1>

                <div className="token-card">

                    <div className="token-label">
                        YOUR TOKEN
                    </div>

                    <div className="token-number">
                        #{patientDashboard.tokenNumber}
                    </div>

                    <div
                        className={
                            `status ${String(
                                patientDashboard.status
                            ).toUpperCase()}`
                        }
                    >
                        {patientDashboard.status}
                    </div>

                </div>

                <div className="info-card">

                    <div className="info-row">
                        <span>
                            Patient
                        </span>

                        <strong>
                            {
                                patientDashboard.patientName
                            }
                        </strong>
                    </div>

                    <div className="info-row">
                        <span>
                            Doctor
                        </span>

                        <strong>
                            {
                                patientDashboard.doctorName
                            }
                        </strong>
                    </div>

                    <div className="info-row">
                        <span>
                            Clinic
                        </span>

                        <strong>
                            {
                                patientDashboard.clinicName
                            }
                        </strong>
                    </div>

                    <div className="info-row">
                        <span>
                            Now Serving
                        </span>

                        <strong>
                            {
                                patientDashboard.currentToken
                                    ? `#${patientDashboard.currentToken}`
                                    : "Not started"
                            }
                        </strong>
                    </div>

                    <div className="info-row">
                        <span>
                            Patients Ahead
                        </span>

                        <strong>
                            {
                                patientDashboard.patientsAhead
                            }
                        </strong>
                    </div>

                    <div className="info-row">
                        <span>
                            Estimated Wait
                        </span>

                        <strong>
                            {
                                patientDashboard.estimatedWaitMinutes
                            }{" "}
                            min
                        </strong>
                    </div>

                </div>

                {patientDashboard.status ===
                    "WAITING" && (

                        <button
                            className="cancel-button"
                            onClick={
                                cancelMyQueue
                            }
                        >
                            CANCEL MY QUEUE
                        </button>
                    )}

                {message && (
                    <div className="message">
                        {message}
                    </div>
                )}

                <button
                    className="next-button"
                    onClick={resetPatient}
                >
                    JOIN ANOTHER QUEUE
                </button>

                <div className="auto-refresh">
                    🟢 Live queue updates
                </div>

            </div>
        );
    };

    // =========================================================
    // DOCTOR DASHBOARD
    // =========================================================

    const renderDoctorDashboard = () => {

        if (!doctorDashboard) {

            return (
                <div className="dashboard">

                    <div className="doctor-account">

                        <h1>
                            Doctor Dashboard
                        </h1>

                        <p>
                            Welcome,{" "}
                            <strong>
                                {authUser?.name}
                            </strong>
                        </p>

                        <p>
                            {authUser?.email}
                        </p>

                    </div>

                    {message && (
                        <div className="message">
                            {message}
                        </div>
                    )}

                    <div className="loading">
                        Loading doctor dashboard...
                    </div>

                </div>
            );
        }

        const waitingQueue =
            doctorQueue.filter(
                entry =>
                    String(entry.status)
                        .toUpperCase() ===
                    "WAITING"
            );

        const servingQueue =
            doctorQueue.filter(
                entry =>
                    String(entry.status)
                        .toUpperCase() ===
                    "SERVING"
            );

        const skippedQueue =
            doctorQueue.filter(
                entry =>
                    String(entry.status)
                        .toUpperCase() ===
                    "SKIPPED"
            );

        return (
            <div className="dashboard doctor-dashboard">

                {/* DOCTOR ACCOUNT */}

                <div className="doctor-account">

                    <h1>
                        Doctor Dashboard
                    </h1>

                    <div>
                        Doctor:{" "}
                        <strong>
                            {
                                doctorDashboard.doctorName ||
                                authUser?.name
                            }
                        </strong>
                    </div>

                    <div>
                        Email:{" "}
                        <strong>
                            {authUser?.email}
                        </strong>
                    </div>

                </div>

                {/* CURRENT PATIENT */}

                <div className="section-title">
                    CURRENT PATIENT
                </div>

                {servingQueue.length > 0 ? (

                    <div className="current-patient-card">

                        <div className="doctor-token">
                            #
                            {
                                servingQueue[0]
                                    .tokenNumber
                            }
                        </div>

                        <div className="patient-name">
                            {
                                servingQueue[0]
                                    .patientName
                            }
                        </div>

                        <div className="serving-badge">
                            SERVING
                        </div>

                        <button
                            className="cancel-button"
                            onClick={
                                skipPatient
                            }
                            disabled={loading}
                        >
                            SKIP PATIENT
                        </button>

                        <button
                            className="complete-button"
                            onClick={
                                completePatient
                            }
                            disabled={loading}
                        >
                            {
                                loading
                                    ? "Processing..."
                                    : "COMPLETE PATIENT"
                            }
                        </button>

                    </div>

                ) : (

                    <div className="empty-card">

                        <div className="empty-title">
                            No patient currently being
                            served
                        </div>

                        <button
                            className="next-button"
                            onClick={
                                startNextPatient
                            }
                            disabled={
                                loading ||
                                waitingQueue.length === 0
                            }
                        >
                            {
                                loading
                                    ? "Processing..."
                                    : waitingQueue.length === 0
                                        ? "NO WAITING PATIENTS"
                                        : "NEXT PATIENT"
                            }
                        </button>

                    </div>
                )}

                {/* MESSAGE */}

                {message && (
                    <div className="message">
                        {message}
                    </div>
                )}

                {/* =========================================================
    TODAY'S STATISTICS
========================================================= */}

                <div className="section-title">
                    TODAY'S STATISTICS
                </div>

                <div className="stats">

                    <div className="stat-card">
        <span>
            Total Today
        </span>

                        <strong>
                            {
                                doctorStatistics?.totalPatientsToday ??
                                0
                            }
                        </strong>
                    </div>

                    <div className="stat-card">
        <span>
            Waiting
        </span>

                        <strong>
                            {
                                doctorStatistics?.waitingPatients ??
                                0
                            }
                        </strong>
                    </div>

                    <div className="stat-card">
        <span>
            Serving
        </span>

                        <strong>
                            {
                                doctorStatistics?.servingPatients ??
                                0
                            }
                        </strong>
                    </div>

                    <div className="stat-card">
        <span>
            Completed
        </span>

                        <strong>
                            {
                                doctorStatistics?.completedPatients ??
                                0
                            }
                        </strong>
                    </div>

                    <div className="stat-card">
        <span>
            Skipped
        </span>

                        <strong>
                            {
                                doctorStatistics?.skippedPatients ??
                                0
                            }
                        </strong>
                    </div>

                    <div className="stat-card">
        <span>
            Cancelled
        </span>

                        <strong>
                            {
                                doctorStatistics?.cancelledPatients ??
                                0
                            }
                        </strong>
                    </div>

                    <div className="stat-card">
        <span>
            Avg. Estimated Wait
        </span>

                        <strong>
                            {
                                doctorStatistics
                                    ?.averageEstimatedWaitMinutes
                                ?? 0
                            }{" "}
                            min
                        </strong>
                    </div>

                </div>

                {/* WAITING QUEUE */}

                <div className="section-title queue-title">
                    WAITING QUEUE
                </div>

                {waitingQueue.length === 0 ? (

                    <div className="empty-queue">
                        No patients waiting
                    </div>

                ) : (

                    <div className="queue-list">

                        {waitingQueue.map(
                            patient => {

                                const id =
                                    patient.queueId ??
                                    patient.id;

                                return (
                                    <div
                                        className="queue-item"
                                        key={id}
                                    >

                                        <div className="queue-token">
                                            #
                                            {
                                                patient.tokenNumber
                                            }
                                        </div>

                                        <div className="queue-info">

                                            <strong>
                                                {
                                                    patient.patientName
                                                }
                                            </strong>

                                            <span>
                                                {
                                                    patient.patientsAhead
                                                }
                                                {" ahead • "}
                                                {
                                                    patient.estimatedWaitMinutes
                                                }
                                                {" min"}
                                            </span>

                                        </div>

                                        <button
                                            className="cancel-button"
                                            onClick={() =>
                                                cancelPatient(
                                                    id
                                                )
                                            }
                                        >
                                            Cancel
                                        </button>

                                    </div>
                                );
                            }
                        )}

                    </div>
                )}

                {/* SKIPPED */}

                <div className="section-title queue-title">
                    SKIPPED PATIENTS
                </div>

                {skippedQueue.length === 0 ? (

                    <div className="empty-queue">
                        No skipped patients
                    </div>

                ) : (

                    <div className="queue-list">

                        {skippedQueue.map(
                            patient => {

                                const id =
                                    patient.queueId ??
                                    patient.id;

                                return (
                                    <div
                                        className="queue-item"
                                        key={id}
                                    >

                                        <div className="queue-token">
                                            #
                                            {
                                                patient.tokenNumber
                                            }
                                        </div>

                                        <div className="queue-info">

                                            <strong>
                                                {
                                                    patient.patientName
                                                }
                                            </strong>

                                            <span>
                                                Patient skipped
                                            </span>

                                        </div>

                                        <button
                                            className="complete-button"
                                            onClick={() =>
                                                completeSkippedPatient(
                                                    id
                                                )
                                            }
                                            disabled={
                                                loading
                                            }
                                        >
                                            MARK COMPLETE
                                        </button>

                                    </div>
                                );
                            }
                        )}

                    </div>
                )}

                {/* COMPLETED TODAY */}

                <div className="section-title queue-title">
                    TODAY'S COMPLETED PATIENTS
                </div>

                {completedToday.length === 0 ? (

                    <div className="empty-queue">
                        No patients completed today
                    </div>

                ) : (

                    <div className="queue-list">

                        {completedToday.map(
                            patient => (

                                <div
                                    className="queue-item"
                                    key={
                                        patient.queueId
                                    }
                                >

                                    <div className="queue-token">
                                        #
                                        {
                                            patient.tokenNumber
                                        }
                                    </div>

                                    <div className="queue-info">

                                        <strong>
                                            {
                                                patient.patientName
                                            }
                                        </strong>

                                        <span>
                                            Completed
                                        </span>

                                    </div>

                                    <div className="completed-badge">
                                        COMPLETED
                                    </div>

                                </div>
                            )
                        )}

                    </div>
                )}

                <div className="auto-refresh">
                    🟢 Live queue updates
                </div>

            </div>
        );
    };

    // =========================================================
    // AUTH SCREEN
    // =========================================================

    if (!authUser) {

        if (authScreen === "register") {

            return (
                <Register
                    onRegistered={user => {

                        setAuthUser(user);

                        setAuthScreen(
                            "login"
                        );
                    }}

                    onSwitchToLogin={() =>
                        setAuthScreen(
                            "login"
                        )
                    }
                />
            );
        }

        return (
            <Login
                onLogin={user => {

                    setAuthUser(user);

                }}

                onSwitchToRegister={() =>
                    setAuthScreen(
                        "register"
                    )
                }
            />
        );
    }

    // =========================================================
    // ROLE
    // =========================================================



    const role =
        String(
            authUser.role || ""
        ).toUpperCase();

    // =========================================================
    // MAIN
    // =========================================================

    return (
        <div className="app">

            {/* HEADER */}

            <div className="header">

                <div>
                    <h1>
                        Doctor Queue
                    </h1>

                    <small>
                        Welcome,{" "}
                        <strong>
                            {authUser.name}
                        </strong>
                    </small>
                </div>

                <div className="switch">

                    {/* PATIENT */}

                    {role === "PATIENT" && (

                        <button
                            className="active"
                        >
                            Patient
                        </button>
                    )}

                    {/* DOCTOR */}

                    {role === "DOCTOR" && (

                        <button
                            className="active"
                        >
                            Doctor
                        </button>
                    )}

                    <button
                        onClick={logout}
                    >
                        Logout
                    </button>

                </div>

            </div>

            {/* CONTENT */}

            {role === "PATIENT" && (

                queueId
                    ? renderPatientDashboard()
                    : renderJoinQueue()
            )}

            {role === "DOCTOR" && (
                renderDoctorDashboard()
            )}

            {role !== "PATIENT" &&
                role !== "DOCTOR" && (

                    <div className="dashboard">

                        <h1>
                            Invalid account role
                        </h1>

                        <button
                            onClick={logout}
                        >
                            Logout
                        </button>

                    </div>
                )}

        </div>
    );
}

export default App;