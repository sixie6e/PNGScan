from PIL import Image
import pytesseract
import os
import re

search_string = input("Text: ")
screenshots_folder = r"/your/folder/path/here"

def pngscan(folder_path, search_query):	
    # ext = ('.png')
    results = []
    print(f"Scanning {folder_path} for '{search_query}'\n")

    for filename in os.listdir(folder_path):
            image_path = os.path.join(folder_path, filename)            
            try:
                img = Image.open(image_path)
                text = pytesseract.image_to_string(img)                
                if search_query in text:
                    results.append(filename)
                    print(f"[+] Found: {filename}")                    
            except Exception as e:
                print(f"[-]{filename}: {e}")

pngscan(screenshots_folder, search_string)
