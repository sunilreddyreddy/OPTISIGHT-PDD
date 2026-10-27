from ultralytics import YOLO

def train_model():
    # Load a pretrained YOLO11n model
    model = YOLO("yolo11n.pt")

    # Train the model
    # Note: 'data' should point to your data.yaml file
    results = model.train(
        data="Dataset/data.yaml",
        epochs=50,
        imgsz=640,
        device='cpu',  # Switched to CPU as GPU (CUDA) is not available
        project="OptiSightModel",
        name="vision_analysis"
    )

    print("Training complete. Exporting to TFLite for Android...")

    # Export the model to TFLite format
    # format='tflite' will generate a .tflite file optimized for mobile
    model.export(format="tflite", int8=True) # int8=True for edge optimization

if __name__ == "__main__":
    train_model()
