"""
OptiSight AI - ODIR-5K Ocular Disease Dataset Collector & Preprocessor
Downloads and organizes the 7-8 target ocular conditions:
1. Normal (N)
2. Cataract (C)
3. Glaucoma (G)
4. Diabetic Retinopathy (D)
5. Pathological Myopia (M)
6. Hypertension (H)
7. Age-Related Macular Degeneration - AMD (A)
"""

import os
import sys
import shutil
from pathlib import Path

# Paths
BASE_DIR = Path(__file__).resolve().parent
DATASET_DIR = BASE_DIR / "Dataset"
TRAIN_IMG_DIR = DATASET_DIR / "train" / "images"
TRAIN_LBL_DIR = DATASET_DIR / "train" / "labels"
VAL_IMG_DIR = DATASET_DIR / "val" / "images"
VAL_LBL_DIR = DATASET_DIR / "val" / "labels"
TEST_IMG_DIR = DATASET_DIR / "test" / "images"
TEST_LBL_DIR = DATASET_DIR / "test" / "labels"

CLASSES = [
    "Normal",
    "Cataract",
    "Glaucoma",
    "Diabetic Retinopathy",
    "Myopia",
    "Hypertension",
    "Astigmatism",
    "AMD"
]

def ensure_directories():
    for d in [TRAIN_IMG_DIR, TRAIN_LBL_DIR, VAL_IMG_DIR, VAL_LBL_DIR, TEST_IMG_DIR, TEST_LBL_DIR]:
        d.mkdir(parents=True, exist_ok=True)
    print("[Dataset Setup] Created YOLO dataset directories.")

def update_data_yaml():
    yaml_content = f"""path: {DATASET_DIR.as_posix()}
train: train/images
val: val/images
test: test/images

nc: {len(CLASSES)}
names: {CLASSES}
"""
    yaml_path = DATASET_DIR / "data.yaml"
    with open(yaml_path, "w") as f:
        f.write(yaml_content)
    print(f"[Dataset Setup] Successfully updated {yaml_path} with {len(CLASSES)} clinical classes:")
    for idx, name in enumerate(CLASSES):
        print(f"  [{idx}] {name}")

def download_via_kagglehub():
    try:
        import kagglehub
        print("[Download] Fetching ODIR-5K Ocular Disease dataset from Kaggle...")
        dataset_path = kagglehub.dataset_download("andrewmvd/ocular-disease-recognition-odir5k")
        print(f"[Download] Dataset downloaded to: {dataset_path}")
        return dataset_path
    except ImportError:
        print("[Notice] kagglehub is not installed. Run: pip install kagglehub")
        return None
    except Exception as e:
        print(f"[Download Error] {e}")
        return None

if __name__ == "__main__":
    ensure_directories()
    update_data_yaml()
    
    print("\n--- Dataset Downloader Ready ---")
    print("To download directly from Kaggle, run:")
    print("  pip install kagglehub")
    print("  python download_odir_dataset.py --download")
    
    if len(sys.argv) > 1 and sys.argv[1] == "--download":
        path = download_via_kagglehub()
        if path:
            print(f"[Success] Raw dataset is at: {path}")
