from pandas import read_csv
from pandas import DataFrame
import pandas as pd
import numpy as np
from sklearn.decomposition import PCA


def read_from_csv():
    # Notice the r before the string. Since the slashes are special characters, prefixing the string with a r will escape the whole string.
    # files_location = r'D:\MASTER\1A\IRES\PythonProjects\ServerSideProject\Files'
    files_location = r'/home/mobilitat/2018/Python/data/interim'
    files_location = r'/home/rigo/mobilitatApp-backend/Python/data/interim'
    label_file = r'/labels.csv'
    accx_file = r'/accx.csv'
    accy_file = r'/accy.csv'
    accz_file = r'/accz.csv'
    accxln_file = r'/acclnx.csv'
    accyln_file = r'/acclny.csv'
    acczln_file = r'/acclnz.csv'

    # We will read from the CSV with the following method, we put the filepath and we set header to none so that the first row it is not set as a header
    # The method will read and convert to a DataFrame

    accx = pd.read_csv(files_location + accx_file, header=None)
    #There are some positions where for some reason there are NaN, this would give problems when computing pca so we will remove the rows containing NaN
    #In the accelerometer values there is a function 'dropna() that allows you to do that
    #However, in the labels there are no such NaN, so we have to identify in which rows are NaN values on the acc files and manually drop the rows of labels
    nan_positions = np.where(np.isnan(accx)) #Detect the position in which there are NaN
    nan_positions = np.array(nan_positions)
    nan_positions = nan_positions[:1,]
    nan_positions = np.unique(nan_positions)
    accx = accx.dropna()
    accx = accx.values
    #accx = accx.transpose()
    #accx = accx.drop(accx.index[nan_positions])
    accy = pd.read_csv(files_location + accy_file, header=None)
    accy = accy.dropna()
    accy = accy.values
    #accy = accy.transpose()
    accz = pd.read_csv(files_location + accz_file, header=None)
    accz = accz.dropna()
    accz = accz.values
    #accz = accz.transpose()
    labels = pd.read_csv(files_location + label_file, header=None)
    labels = labels.drop(labels.index[nan_positions]) #Drop rows where there are Nan values
    # We don't need to work with dataframes so we obtain the values and the axes will be removed
    labels = labels.values
    #labels = labels.transpose()

    acclnx = pd.read_csv(files_location + accxln_file, header=None)
    acclnx = acclnx.dropna()
    acclnx = acclnx.values
    acclny = pd.read_csv(files_location + accyln_file, header=None)
    acclny = acclny.dropna()
    acclny = acclny.values
    acclnz = pd.read_csv(files_location + acczln_file, header=None)
    acclnz = acclnz.dropna()
    acclnz = acclnz.values

    return labels, accx, accy, accz, acclnx, acclny, acclnz

def read_pca_from_csv(pca_file):
    files_location = r'/home/mobilitat/2018/Python/data'+'/interim'
    pca_values = pd.read_csv(files_location + pca_file, header=None)
    pca_values = pca_values.values
    return pca_values

def write_to_csv(file,data):
    dataFrame = DataFrame(data)
    files_location = r'/home/mobilitat/2018/Python/data'+'/interim'
    dataFrame.to_csv(files_location + file, index= None, header=False)

def write_to_excel(file,data):
    dataFrame = DataFrame(data)
    files_location = r'/home/mobilitat/2018/Python/data'+'/interim'
    writer = pd.ExcelWriter(files_location + file, engine='xslwriter')
    dataFrame.to_excel(writer, sheet_name='Sheet_1')
    writer.save()

#Method that computes the mean
def mean_data():
    mean_accX = np.mean(accx, axis=1) #'axis=1' to compute the mean on each row, not each column
    mean_accY = np.mean(accy, axis=1)
    mean_accZ = np.mean(accz, axis=1)
    return mean_accX, mean_accY, mean_accZ

#Method that computes the standard deviation
def std_data():
    std_accX = np.std(accx, axis=1)
    std_accY = np.std(accy, axis=1)
    std_accZ = np.std(accz, axis=1)
    return std_accX, std_accY, std_accZ

#Method that computes the first component of the Principal Component Analysis
def pca_data():
    pca = PCA(n_components=1)
    #principalDf = pd.DataFrame(data=principalComponents_accx, columns=['principal component 1'])
    pca_accX = pca.fit_transform(accx)
    pca_accY = pca.fit_transform(accy)
    pca_accZ = pca.fit_transform(accz)
    return pca_accX, pca_accY, pca_accZ



#------------------------- Initialization of variables --------------------

file = ""
train_data = []
files_location = r'/home/mobilitat/2018/Python/data'

#------------------------ MAIN CODE -----------------------------------------

# ------ Obtaining the parameters -------
labels, accx, accy, accz, acclnx, acclny, acclnz = read_from_csv()

mean_accX, mean_accY, mean_accZ = mean_data()
std_accX, std_accY, std_accZ = std_data()
pca_accX, pca_accY, pca_accZ = pca_data()

mean_acclnX, mean_acclnY, mean_acclnZ = mean_data()
std_acclnX, std_acclnY, std_acclnZ = std_data()
pca_acclnX, pca_acclnY, pca_acclnZ = pca_data()

print('MEAN_ACC_X')
print(mean_accX)
print('PCA_ACC_X')
print(pca_accX)

# ------ Preparing the data ---------
pca_accX = pca_accX.transpose()
#The pca parameter is inside two brackets [[]], this is a list of one item being the item a list. We only want to have a list, that is why we access the first position
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
train_data.append(std_accX)
train_data.append(std_accY)
train_data.append(std_accZ)
train_data.append(pca_accX)
train_data.append(pca_accY)
train_data.append(pca_accZ)
train_data.append(mean_acclnX)
train_data.append(mean_acclnY)
train_data.append(mean_acclnZ)
train_data.append(std_acclnX)
train_data.append(std_acclnY)
train_data.append(std_acclnZ)
train_data.append(pca_acclnX)
train_data.append(pca_acclnY)
train_data.append(pca_acclnZ)
train_data.append(labels)


# ----- Writing to csv ---------
# print('TRAIN DATA')
# print(train_data)
# print('TRAIN DATA TRANSPOSED')
train_data = np.transpose(train_data)
#print(train_data)
write_to_csv(files_location+'/processed/Train_data.csv', train_data)






