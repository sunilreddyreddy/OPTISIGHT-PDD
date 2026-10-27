// frontend/src/firebase.js
import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import { getFirestore } from "firebase/firestore";

// OptiSight AI - Live Firebase Configuration
const firebaseConfig = {
  apiKey: "AIzaSyDCeO3OtoB5FLFSV7FkLOmfwmkbifXb-hs",
  authDomain: "optisight-1d9e3.firebaseapp.com",
  projectId: "optisight-1d9e3",
  storageBucket: "optisight-1d9e3.firebasestorage.app",
  messagingSenderId: "1026863627118",
  appId: "1:1026863627118:web:089a19a8ab529b2dea1f7f",
  measurementId: "G-PNLDVVPSN6"
};

// Initialize Firebase App
const app = initializeApp(firebaseConfig);

// Initialize and export Firebase Auth & Firestore services
export const auth = getAuth(app);
export const db = getFirestore(app);
export default app;
