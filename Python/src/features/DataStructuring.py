from pandas import read_csv
from pandas import DataFrame
import pandas as pd
import numpy as np
from sklearn.decomposition import PCA
from sklearn.model_selection import train_test_split

# Reads the data stored on data/interim
ACCLNZ_T_CSV = r'acclnz_t.csv'
ACCLNY_T_CSV = r'acclny_t.csv'
ACCLNX_T_CSV = r'acclnx_t.csv'
ACCZ_T_CSV = r'accz_t.csv'
ACCY_T_CSV = r'accy_t.csv'
ACCX_T_CSV = r'accx_t.csv'
LABELS_T_CSV = r'labels_t.csv'


def read_from_csv():
    # Notice the r before the string. Since the slashes are special characters, prefixing the string with a r will escape the whole string.
    # files_location = r'D:\MASTER\1A\IRES\PythonProjects\ServerSideProject\Files'
    files_location = r'/mobilitatApp-backend/Python/data/interim'
    label_file = r'/labels_t.csv'
    accx_file = r'/accx_t.csv'
    accy_file = r'/accy_t.csv'
    accz_file = r'/accz_t.csv'
    accxln_file = r'/acclnx_t.csv'
    accyln_file = r'/acclny_t.csv'
    acczln_file = r'/acclnz_t.csv'

    # We will read from the CSV with the following method, we put the filepath and we set header to none so that the first row it is not set as a header
    # The method will read and convert to a DataFrame
    accx = pd.read_csv(files_location + accx_file, header=None, dtype=np.float64)
    #There are some positions where for some reason there are NaN, this would give problems when computing pca so we will remove the rows containing NaN
    #In the accelerometer values there is a function 'dropna() that allows you to do that
    #However, in the labels there are no such NaN, so we have to identify in which rows are NaN values on the acc files and manually drop the rows of labels
    accx = accx.replace([np.inf, -np.inf], np.nan)
    nan_positions = np.where(np.isnan(accx)) #Detect the position in which there are NaN
    nan_positions = np.array(nan_positions)
    nan_positions = nan_positions[:1,]
    nan_positions = np.unique(nan_positions)
    accx = accx.dropna()
    accx = accx.values
    #accx = accx.transpose()
    #accx = accx.drop(accx.index[nan_positions])
    accy = pd.read_csv(files_location + accy_file, header=None, dtype=np.float64)
    accy = accy.replace([np.inf, -np.inf], np.nan)
    accy = accy.dropna()
    accy = accy.values
    #accy = accy.transpose()
    accz = pd.read_csv(files_location + accz_file, header=None, dtype=np.float64)
    accz = accz.replace([np.inf, -np.inf], np.nan)
    accz = accz.dropna()
    accz = accz.values
    #accz = accz.transpose()
    labels = pd.read_csv(files_location + label_file, header=None)
    labels = labels.drop(labels.index[nan_positions]) #Drop rows where there are Nan values
    # We don't need to work with dataframes so we obtain the values and the axes will be removed
    labels = labels.values
    #labels = labels.transpose()

    acclnx = pd.read_csv(files_location + accxln_file, header=None, dtype=np.float64)
    acclnx = acclnx.replace([np.inf, -np.inf], np.nan)
    acclnx = acclnx.dropna()
    acclnx = acclnx.values
    acclny = pd.read_csv(files_location + accyln_file, header=None, dtype=np.float64)
    acclny = acclny.replace([np.inf, -np.inf], np.nan)
    acclny = acclny.dropna()
    acclny = acclny.values
    acclnz = pd.read_csv(files_location + acczln_file, header=None, dtype=np.float64)
    acclnz = acclnz.replace([np.inf, -np.inf], np.nan)
    acclnz = acclnz.dropna()
    acclnz = acclnz.values

    return labels, accx, accy, accz, acclnx, acclny, acclnz


def read_pca_from_csv(pca_file):
    files_location = r'/home/rigo/mobilitatApp-backend/Python/data'+'/interim'
    pca_values = pd.read_csv(files_location + pca_file, header=None)
    pca_values = pca_values.values
    return pca_values


def write_to_csv(file, data):
    dataFrame = DataFrame(data)
    files_location = r'/home/rigo/mobilitatApp-backend/Python/data'
    dataFrame.to_csv(files_location + file, index= None, header=False)


def write_to_excel(file,data):
    dataFrame = DataFrame(data)
    files_location = r'/home/rigo/mobilitatApp-backend/Python/data'
    writer = pd.ExcelWriter(files_location + file, engine='xslwriter')
    dataFrame.to_excel(writer, sheet_name='Sheet_1')
    writer.save()


#Method that computes the mean
def mean_data(x, y, z):
    mean_accX = np.mean(x, axis=1) #'axis=1' to compute the mean on each row, not each column
    mean_accY = np.mean(y, axis=1)
    mean_accZ = np.mean(z, axis=1)
    return mean_accX, mean_accY, mean_accZ


#Method that computes the standard deviation
def std_data(x, y, z):
    std_accX = np.std(x, axis=1)
    std_accY = np.std(y, axis=1)
    std_accZ = np.std(z, axis=1)
    return std_accX, std_accY, std_accZ


#Method that computes the first component of the Principal Component Analysis
def pca_data(x, y, z):
    pca = PCA(n_components=1)
    #principalDf = pd.DataFrame(data=principalComponents_accx, columns=['principal component 1'])
    pca_accX = pca.fit_transform(x)
    pca_accY = pca.fit_transform(y)
    pca_accZ = pca.fit_transform(z)
    return pca_accX, pca_accY, pca_accZ


def data_structuring(input_file, out_put_file_training, out_put_file_testing):
    # ------------------------- Initialization of variables --------------------

    file = ""
    train_data = []
    test_data = []
    files_location = input_file

    # ------------------------ MAIN CODE -----------------------------------------

    # ------ Obtaining the parameters -------
    labels, accx, accy, accz, acclnx, acclny, acclnz = read_from_csv()

    mean_accX, mean_accY, mean_accZ = mean_data(accx,accy,accz)
    std_accX, std_accY, std_accZ = std_data(accx,accy,accz)
    pca_accX, pca_accY, pca_accZ = pca_data(accx,accy,accz)

    mean_acclnX, mean_acclnY, mean_acclnZ = mean_data(acclnx, acclny, acclnz)
    std_acclnX, std_acclnY, std_acclnZ = std_data(acclnx, acclny, acclnz)
    pca_acclnX, pca_acclnY, pca_acclnZ = pca_data(acclnx, acclny, acclnz)


    # ------ Preparing the data ---------
    pca_accX = pca_accX.transpose()
    # The pca parameter is inside two brackets [[]], this is a list of one item being the item a list. We only want to have a list, that is why we access the first position
    pca_accX = pca_accX[0]
    pca_accY = pca_accY.transpose()
    pca_accY = pca_accY[0]
    pca_accZ = pca_accZ.transpose()
    pca_accZ = pca_accZ[0]
    pca_acclnX = pca_acclnX.transpose()
    pca_acclnX = pca_acclnX[0]
    pca_acclnY = pca_acclnY.transpose()
    pca_acclnY = pca_acclnY[0]
    pca_acclnZ = pca_acclnZ.transpose()
    pca_acclnZ = pca_acclnZ[0]

    labels = labels.transpose()
    labels = labels[0]

    # ------- Creating a matrix containing all the parameters ----------
    # We have the data needed but each parameter is in an array, we have to combine the arrays to form a matrix

    train_data.append(mean_accX)
    train_data.append(mean_accY)
    train_data.append(mean_accZ)
    #train_data.append(std_accX)
    #train_data.append(std_accY)
    #train_data.append(std_accZ)
    #train_data.append(pca_accX)
    #train_data.append(pca_accY)
    #train_data.append(pca_accZ)
    #train_data.append(mean_acclnX)
    #train_data.append(mean_acclnY)
    #train_data.append(mean_acclnZ)
    #train_data.append(std_acclnX)
    #train_data.append(std_acclnY)
    #train_data.append(std_acclnZ)
    #train_data.append(pca_acclnX)
    #train_data.append(pca_acclnY)
    #train_data.append(pca_acclnZ)
    train_data.append(labels)

    # ----- Writing to csv ---------
    # print('TRAIN DATA')
    # print(train_data)
    # print('TRAIN DATA TRANSPOSED')
    train_data = np.transpose(train_data)

    # ----- Split onto training and test ---------
    train_data, test_data = train_test_split(train_data, test_size=0.4, train_size=0.6)

    # print(train_data)
    write_to_csv(out_put_file_training, train_data)
    write_to_csv(out_put_file_testing, test_data)

# Change input file to test this script and take care of some file names at the code
data_structuring(input_file=r'/mobilitatApp-backend/Python/data',
                                 out_put_file_training='/processed/Train_data.csv',
                                 out_put_file_testing='/processed/Test_data.csv')
