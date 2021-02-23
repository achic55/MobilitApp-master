import os.path
import pickle
import EnsembleBaggedTreesTrain as Name
import numpy
print(Name.__name__)

package_name = os.path.dirname(Name.__name__)
file_name = "modelV0.sav"
joined_path = os.path.join(package_name, file_name)

model_file = open(joined_path, 'rb')
model = pickle.load(model_file)
print(model)

list = [[1, 2, 3], [0, 1, 2]];
numpy_list = numpy.array(list)
numpy_list.