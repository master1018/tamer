echo "Init..."
rm ./result/res
rm ./result/output
rm ./result/output2 
rm ./result/res_graph*
rm -rf ./result/exp_data/ 
rm ./tmp/sem
rm -rf ./data/input/
mkdir ./data/input/
rm ./data/remote_data/
touch ./tmp/sem
mkdir ./result/exp_data/
echo 1 > ./tmp/sem
echo "Init success,begin to start:"

streamlit run main.py
