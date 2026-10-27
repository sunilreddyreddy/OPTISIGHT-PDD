import os

def check_folder(path, folder_name):
    images_path = os.path.join(path, folder_name, 'images')
    labels_path = os.path.join(path, folder_name, 'labels')

    if not os.path.exists(images_path) or not os.path.exists(labels_path):
        print(f"Error: {folder_name} images or labels folder not found!")
        return

    images = [f for f in os.listdir(images_path) if f.endswith(('.jpg', '.png', '.jpeg'))]
    labels = [f for f in os.listdir(labels_path) if f.endswith('.txt')]

    print(f"--- {folder_name.upper()} SET ---")
    print(f"Images found: {len(images)}")
    print(f"Labels found: {len(labels)}")

    # Check if every image has a corresponding label
    image_basenames = {os.path.splitext(f)[0] for f in images}
    label_basenames = {os.path.splitext(f)[0] for f in labels}

    missing_labels = image_basenames - label_basenames
    missing_images = label_basenames - image_basenames

    if not missing_labels and not missing_images:
        print("Consistency check: PASSED (All images have labels and vice versa)")
    else:
        if missing_labels:
            print(f"Warning: {len(missing_labels)} images are missing labels!")
        if missing_images:
            print(f"Warning: {len(missing_images)} labels are missing images!")

if __name__ == "__main__":
    dataset_path = "Dataset"
    for folder in ['train', 'val', 'test']:
        check_folder(dataset_path, folder)
