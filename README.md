# Common Dental Disease Detection & Management Among Children using Deep Learning

A mobile application that uses deep learning to detect and manage common dental diseases in children from images captured via the device camera or uploaded from the gallery. The application helps parents or guardians receive early detection and management advice based on AI-powered analysis.

# 📱 Features
- Detect common dental diseases using a trained CNN (ResNet-based) model.
- Supports image capture and upload.
- Shows disease name and treatment recommendations.
- Stores child profiles and scan history.
- Filter detection reports by child name.
- Firebase integration for:
  - Authentication
  - Cloud Firestore (child profiles, detection records)
  - Firebase Storage (image uploads)
- User-friendly dashboard with scan statistics and charts.

# 🧠 Model Development
- Trained on a custom dental image dataset.
- Compared six models:
  - CNN, RNN, DBN
  - ResNet, Inception, DenseNet (CNN variants)
- ResNet-based CNN selected for final deployment due to highest accuracy and robustness.
- Converted to TensorFlow Lite (.tflite) for on-device inference.

# 🛠️ Tech Stack
- Language: Java & XML
- IDE: Android Studio
- Model Framework: TensorFlow Lite
- Backend: Firebase Authentication, Firestore, Firebase Storage

# 📊 Dashboard Overview
- Total children profiles
- Total scans conducted
- Last detected condition & suggested treatment
- Line chart of scan activity over time

# 🚀 How to Run
1. Clone the repo
2. Open with Android Studio
3. Connect a device/emulator
4. Replace Firebase config (google-services.json)
5. Run the app

# 🔐 Firebase Setup
- Enable:
  - Authentication (Email/Password)
  - Firestore Database
  - Firebase Storage
- Set security rules to protect user-specific data

# 👩‍💻 Developed By
Sarah Syazana,
Bachelor of Computer Science (Hons.),
Universiti Teknologi MARA (UiTM)

# 📄 License
This project is for academic and research purposes only. Not intended for clinical use.
