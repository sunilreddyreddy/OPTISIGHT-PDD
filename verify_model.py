from ultralytics import YOLO
import os

def verify():
    model_path = "runs/detect/OptiSightModel/vision_analysis-4/weights/best.pt"
    if not os.path.exists(model_path):
        print(f"Error: Model not found at {model_path}")
        return

    model = YOLO(model_path)

    # Use an image from the val set
    test_img = "Dataset/val/images/i_9_jpg.rf.0a791ce2fd4b9652f50afcaee80e23f3.jpg"
    if not os.path.exists(test_img):
        print("Test image not found, looking for any image in val...")
        val_imgs = os.listdir("Dataset/val/images")
        if val_imgs:
            test_img = os.path.join("Dataset/val/images", val_imgs[0])
        else:
            print("No images found in val set.")
            return

    print(f"Testing model on: {test_img}")
    results = model(test_img)

    for result in results:
        print("--- Detection Results ---")
        print(result.probs) # If classification
        print(result.boxes) # If detection

        if len(result.boxes) > 0:
            for box in result.boxes:
                cls = int(box.cls[0])
                conf = float(box.conf[0])
                name = model.names[cls]
                print(f"Found: {name} with {conf:.2f} confidence")
        else:
            print("No objects detected.")

if __name__ == "__main__":
    verify()
