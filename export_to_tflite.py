from ultralytics import YOLO
import os

def export():
    model_path = "runs/detect/OptiSightModel/vision_analysis-4/weights/best.pt"
    if not os.path.exists(model_path):
        print(f"Error: Model not found at {model_path}")
        return

    model = YOLO(model_path)
    print("Exporting model to TFLite format...")

    # Export the model
    # imgsz 320 or 640 is typical for mobile.
    # int8=True provides quantization for better performance on mobile.
    model.export(format="tflite", int8=True, imgsz=640)

if __name__ == "__main__":
    export()
