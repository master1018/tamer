echo "Init..."
rm ./result/res
rm ./result/output
rm ./result/res_graph*
rm ./tmp/sem
rm ./data/input/*
touch ./tmp/sem
echo 1 > ./tmp/sem
echo "Init success,begin to start:"

streamlit run main.py
