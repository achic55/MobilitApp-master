import DataStructuring
import DataTreatment
import sys

sys.path.append('/home/rigo/mobilitatApp-backend/Python/src/models')
import EnsembleBaggedTreesTrain

#Change paths depending on system structure
DataTreatment.data_treatment(base_path='/mobilitatApp-backend/Python/data/interim/',
                             input_path='/mobilitatApp-backend/Python/data/raw/')
DataStructuring.data_structuring(input_file=r'/mobilitatApp-backend/Python/data',
                                 out_put_file_training='/processed/Train_data.csv',
                                 out_put_file_testing='/processed/Test_data.csv')
EnsembleBaggedTreesTrain.ensemble_bagged_trees(input_path=r'/mobilitatApp-backend/Python/',
                                               model_path='models/', accuracy_path='models/model_acc.txt',
                                               trained_data_path='data/processed/Train_data.csv',
                                               test_data_path='data/processed/Test_data.csv')
