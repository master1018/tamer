public class test {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            String message = "<JCMessage>" + "<tasktype>Message</tasktype>" + "<TimeStamp>" + data.getTimeStamp() + "</TimeStamp>" + "<Channel>" + (channelBox.getSelectedItem().toString().equals("Random") ? data.getRandomEntry(data.getChannels()) : channelBox.getSelectedItem().toString()) + "</Channel>" + "<location>" + (locationBox.getSelectedItem().toString().equals("Random") ? data.getRandomEntry(data.getLocations()) : locationBox.getSelectedItem().toString()) + "</location>" + "<patient>" + (patientBox.getSelectedItem().toString().equals("Random") ? data.getRandomEntry(data.getPatients()) : patientBox.getSelectedItem().toString()) + "</patient>" + "<device>" + (deviceBox.getSelectedItem().toString().equals("Random") ? data.getRandomEntry(data.getDevices()) : deviceBox.getSelectedItem().toString()) + "</device>" + "<staff>" + (staffBox.getSelectedItem().toString().equals("Random") ? data.getRandomEntry(data.getStaff()) : staffBox.getSelectedItem().toString()) + "</staff>" + "<staffName>" + (staffNameBox.getSelectedItem().toString().equals("Random") ? data.getRandomEntry(data.getStaffNames()) : staffNameBox.getSelectedItem().toString()) + "	</staffName>" + "<information>" + (infoBox.getSelectedItem().toString().equals("Random") ? data.getRandomEntry(data.getInformation()) : infoBox.getSelectedItem().toString()) + "</information>" + "</JCMessage>";
            data.getBuffer().add(message);
            String colData[] = { data.formatString(message) };
            model.insertRow(0, colData);
        }
}
