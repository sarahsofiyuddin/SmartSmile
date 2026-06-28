# 🦷 SmartSmile: Dental Disease Detection & Management App

SmartSmile is a mobile application that leverages deep learning to detect and manage common dental diseases among children. By analysing images captured via the device camera or uploaded from the gallery, the application provides early detection and basic management guidance for parents and guardians.

---

## 🌐 **Live Demo (Try App in Browser)**
https://appetize.io/app/b_7cruiidxejayy4usizrjnihf5u

---

## 🖼️ Project Poster

https://drive.google.com/file/d/1sTxQIizPlaudjEkkccWw7f_55l7HTu5Y/view?usp=sharing

---

## 📱 Key Features
- Detects common dental diseases using a trained deep learning model
- Supports real-time image capture and gallery upload
- Displays predicted disease along with treatment recommendations
- Manages child profiles and stores scan history
- Allows filtering of detection reports by child profile
- Interactive dashboard with scan statistics and visual insights

---

## 🧠 Model Development
- Developed using a custom dental image dataset
- Evaluated multiple models:
  - CNN, RNN, DBN
  - ResNet, Inception, DenseNet (CNN variants)
- ResNet-based CNN selected due to superior performance and robustness
- Model converted to **TensorFlow Lite (TFLite)** for efficient on-device inference

🔗 **View Training Notebook (Google Colab):**  
https://colab.research.google.com/drive/1RuOPZkYy9ts3pffwSEeYUJXY1kvfSqiA?usp=sharing

---

## 🛠️ Tech Stack
- **Mobile Development:** Java, XML (Android Studio)
- **Machine Learning:** TensorFlow Lite
- **Backend Services:**  
  - Firebase Authentication  
  - Cloud Firestore  
  - Firebase Storage

---

## 📊 Dashboard Overview
- Total number of registered child profiles
- Total number of scans conducted
- Latest detected condition with suggested treatment
- Scan activity trends displayed using line charts

---

## 🚀 Getting Started
1. Clone this repository  
2. Open the project in Android Studio  
3. Connect a physical device or emulator  
4. Add your Firebase configuration file (`google-services.json`)  
5. Build and run the application  

---

## 🔐 Firebase Setup
Ensure the following services are enabled:
- Authentication (Email/Password)
- Cloud Firestore
- Firebase Storage  

Configure appropriate security rules to restrict access to user-specific data.

---

## 🎯 Project Highlights
- End-to-end development: **data preprocessing → model training → mobile deployment**
- Integration of AI model into a real-world Android application
- Focus on usability for non-technical users (parents/guardians)

---

## 👩‍💻 Developed By
**Sarah Syazana**  
Bachelor of Computer Science (Hons.), Big Data Analytics  
Universiti Teknologi MARA (UiTM)

---

## ⚠️ Disclaimer
This application is developed for academic and research purposes only and is not intended for clinical diagnosis or professional medical use.
