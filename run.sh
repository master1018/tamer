echo "Init..."
rm ./result/res
rm ./result/output
rm ./result/res_graph*
rm ./result/exp_data/ 
rm ./tmp/sem
rm ./data/input/*
rm ./data/remote_data/
touch ./tmp/sem
mkdir ./result/exp_data/
echo 1 > ./tmp/sem
echo "Init success,begin to start:"

streamlit run main.py
