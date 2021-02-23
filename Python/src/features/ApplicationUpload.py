import subprocess
import os


class ApplicationUpload:
    def __init__(self, project_path):
        self.path = project_path

    # Delete te contents of the build directory
    def clean(self):
        if os.path.isfile(self.path):
            subprocess.run(["chmod", "755", self.path])
            subprocess.run([self.path, "clean"])
        else:
            print("Given path isn't a folder")

    # Task to designate assembling all outputs and runing checks
    def build(self):
        if os.path.isfile(self.path):
            subprocess.run(["chmod", "755", self.path])
            subprocess.run([self.path, "build"])
        else:
            print("Given path isn't a folder")

    def dist(self):
        if os.path.isfile(self.path):
            subprocess.run(["chmod", "755", self.path])
            subprocess.run([self.path, "dist"])
        else:
            print("Given path isn't a folder")


a = ApplicationUpload("/AutoUploadAPPTest/gradlew")
a.clean()
a.dist()