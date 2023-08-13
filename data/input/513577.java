public class MulticastSocketTest extends SocketTestCase {
	Thread t;
	MulticastSocket mss;
	MulticastServer server;
	boolean atLeastOneInterface = false;
	boolean atLeastTwoInterfaces = false;
	private NetworkInterface networkInterface1 = null;
	private NetworkInterface networkInterface2 = null;
	private NetworkInterface IPV6networkInterface1 = null;
	static class MulticastServer extends Thread {
		public MulticastSocket ms;
		boolean running = true;
		volatile public byte[] rbuf = new byte[512];
        volatile DatagramPacket rdp = null;
        private InetAddress groupAddr = null;
        private SocketAddress groupSockAddr = null;
        private NetworkInterface groupNI = null;
        public void run() {
			try {
                byte[] tmpbuf = new byte[512];
                DatagramPacket tmpPack =
                        new DatagramPacket(tmpbuf, tmpbuf.length);
                while (running) {
					try {
                        ms.receive(tmpPack);
                        System.arraycopy(tmpPack.getData(), 0, rdp.getData(),
                                rdp.getOffset(), tmpPack.getLength());
                        rdp.setLength(tmpPack.getLength());
                        rdp.setAddress(tmpPack.getAddress());
                        rdp.setPort(tmpPack.getPort());
                    } catch (java.io.InterruptedIOException e) {
                        Thread.yield();
					}
				}
			} catch (java.io.IOException e) {
				System.out.println("Multicast server failed: " + e);
			} finally {
				ms.close();
			}
		}
		public void stopServer() {
			running = false;
            try {
                if (groupAddr != null) {
                    ms.leaveGroup(groupAddr);
                } else if (groupSockAddr != null) {
                    ms.leaveGroup(groupSockAddr, groupNI);
                }
            } catch (IOException e) {}
        }
        public MulticastServer(InetAddress anAddress, int aPort)
                throws java.io.IOException {
            rbuf = new byte[512];
            rbuf[0] = -1;
            rdp = new DatagramPacket(rbuf, rbuf.length);
            ms = new MulticastSocket(aPort);
            ms.setSoTimeout(2000);
            groupAddr = anAddress;
            ms.joinGroup(groupAddr);
        }
        public MulticastServer(SocketAddress anAddress, int aPort,
				NetworkInterface netInterface) throws java.io.IOException {
			rbuf = new byte[512];
			rbuf[0] = -1;
			rdp = new DatagramPacket(rbuf, rbuf.length);
			ms = new MulticastSocket(aPort);
			ms.setSoTimeout(2000);
            groupSockAddr = anAddress;
            groupNI = netInterface;
            ms.joinGroup(groupSockAddr, groupNI);
		}
	}
	public void test_Constructor() throws IOException {
        MulticastSocket s = new MulticastSocket();
        assertTrue(s.getReuseAddress());
	}
	public void test_ConstructorI() throws IOException {
	    MulticastSocket orig = new MulticastSocket();
        int port = orig.getLocalPort();
        orig.close();
		MulticastSocket dup = null;
		try {
			dup = new MulticastSocket(port);
            assertTrue(dup.getReuseAddress());
		} catch (IOException e) {
			fail("duplicate binding not allowed: " + e);
		}
		if (dup != null)
			dup.close();
	}
	public void test_getInterface() throws Exception {
		assertTrue("Used for testing.", true);
		int groupPort = Support_PortManager.getNextPortForUDP();
                if (atLeastOneInterface) {
                        mss = new MulticastSocket(groupPort);
                        String preferIPv4StackValue = System
                                        .getProperty("java.net.preferIPv4Stack");
                        String preferIPv6AddressesValue = System
                                        .getProperty("java.net.preferIPv6Addresses");
                        if (((preferIPv4StackValue == null) || preferIPv4StackValue
                                        .equalsIgnoreCase("false"))
                                        && (preferIPv6AddressesValue != null)
                                        && (preferIPv6AddressesValue.equals("true"))) {
                                assertEquals("inet Address returned when not set",
                                             InetAddress.getByName("::0"),
                                             mss.getInterface());
                        } else {
                                assertEquals("inet Address returned when not set",
                                             InetAddress.getByName("0.0.0.0"),
                                             mss.getInterface());
                        }
                        Enumeration addresses = networkInterface1.getInetAddresses();
                        if (addresses.hasMoreElements()) {
                                InetAddress firstAddress = (InetAddress) addresses
                                                .nextElement();
                                mss.setInterface(firstAddress);
                                assertEquals("getNetworkInterface did not return interface set by setInterface",
                                             firstAddress, mss.getInterface());
                                groupPort = Support_PortManager.getNextPortForUDP();
                                mss = new MulticastSocket(groupPort);
                                mss.setNetworkInterface(networkInterface1);
                                assertEquals("getInterface did not return interface set by setNetworkInterface",
                                             networkInterface1,
                                             NetworkInterface.getByInetAddress(mss.getInterface()));
                        }
                }
	}
	public void test_getNetworkInterface() throws IOException {
        int groupPort = Support_PortManager.getNextPortForUDP();
        if (atLeastOneInterface) {
            mss = new MulticastSocket(groupPort);
            NetworkInterface theInterface = mss.getNetworkInterface();
            assertTrue(
                    "network interface returned wrong network interface when not set:"
                            + theInterface, theInterface.getInetAddresses()
                            .hasMoreElements());
            InetAddress firstAddress = (InetAddress) theInterface
                    .getInetAddresses().nextElement();
            String preferIPv4StackValue = System
                    .getProperty("java.net.preferIPv4Stack");
            String preferIPv6AddressesValue = System
                    .getProperty("java.net.preferIPv6Addresses");
            if (((preferIPv4StackValue == null) || preferIPv4StackValue
                    .equalsIgnoreCase("false"))
                    && (preferIPv6AddressesValue != null)
                    && (preferIPv6AddressesValue.equals("true"))) {
                assertEquals("network interface returned wrong network interface when not set:"
                             + theInterface,
                             firstAddress, InetAddress.getByName("::0"));
            } else {
                assertEquals("network interface returned wrong network interface when not set:"
                             + theInterface,
                             InetAddress.getByName("0.0.0.0"),
                             firstAddress);
            }
            mss.setNetworkInterface(networkInterface1);
            assertEquals("getNetworkInterface did not return interface set by setNeworkInterface",
                         networkInterface1, mss.getNetworkInterface());
            if (atLeastTwoInterfaces) {
                mss.setNetworkInterface(networkInterface2);
                assertEquals("getNetworkInterface did not return network interface set by second setNetworkInterface call",
                             networkInterface2, mss.getNetworkInterface());
            }
            groupPort = Support_PortManager.getNextPortForUDP();
            mss = new MulticastSocket(groupPort);
            if (IPV6networkInterface1 != null) {
                mss.setNetworkInterface(IPV6networkInterface1);
                assertEquals("getNetworkInterface did not return interface set by setNeworkInterface",
                             IPV6networkInterface1,
                             mss.getNetworkInterface());
            }
            groupPort = Support_PortManager.getNextPortForUDP();
            mss = new MulticastSocket(groupPort);
            Enumeration addresses = networkInterface1.getInetAddresses();
            if (addresses.hasMoreElements()) {
                firstAddress = (InetAddress) addresses.nextElement();
                mss.setInterface(firstAddress);
                assertEquals("getNetworkInterface did not return interface set by setInterface",
                             networkInterface1,
                             mss.getNetworkInterface());
            }
        }
    }
	public void test_getTimeToLive() {
		try {
			mss = new MulticastSocket();
			mss.setTimeToLive(120);
			assertEquals("Returned incorrect 1st TTL",
                                     120, mss.getTimeToLive());
			mss.setTimeToLive(220);
			assertEquals("Returned incorrect 2nd TTL",
                                     220, mss.getTimeToLive());
			ensureExceptionThrownIfOptionIsUnsupportedOnOS(SO_MULTICAST);
		} catch (Exception e) {
			handleException(e, SO_MULTICAST);
		}
	}
	public void test_getTTL() {
		try {
			mss = new MulticastSocket();
			mss.setTTL((byte) 120);
			assertEquals("Returned incorrect TTL",
                                     120, mss.getTTL());
			ensureExceptionThrownIfOptionIsUnsupportedOnOS(SO_MULTICAST);
		} catch (Exception e) {
			handleException(e, SO_MULTICAST);
		}
	}
	public void test_joinGroupLjava_net_InetAddress() throws Exception {
                String msg = null;
		InetAddress group = null;
		int[] ports = Support_PortManager.getNextPortsForUDP(2);
		int groupPort = ports[0];
                group = InetAddress.getByName("224.0.0.3");
                server = new MulticastServer(group, groupPort);
                server.start();
                Thread.sleep(1000);
                msg = "Hello World";
                mss = new MulticastSocket(ports[1]);
                DatagramPacket sdp = new DatagramPacket(msg.getBytes(), msg
                                .length(), group, groupPort);
                mss.send(sdp, (byte) 10);
                Thread.sleep(1000);
                assertEquals("Group member did not recv data",
                             msg,
                             new String(server.rdp.getData(), 0, server.rdp.getLength()));
	}
    @KnownFailure("Fails in CTS but passes under run-core-tests")
	public void test_joinGroupLjava_net_SocketAddressLjava_net_NetworkInterface() throws IOException, InterruptedException {
		class mySecurityManager extends SecurityManager {
			public void checkMulticast(InetAddress address) {
				throw new SecurityException("not allowed");
			}
		}
		String msg = null;
		InetAddress group = null;
		SocketAddress groupSockAddr = null;
		int[] ports = Support_PortManager.getNextPortsForUDP(2);
		int groupPort = ports[0];
		int serverPort = ports[1];
        Enumeration<NetworkInterface> theInterfaces = NetworkInterface.getNetworkInterfaces();
        mss = new MulticastSocket(groupPort);
        try {
            mss.joinGroup(null, null);
            fail("Did not get exception when group was null");
        } catch (IllegalArgumentException e) {
        }
        try {
            groupSockAddr = new InetSocketAddress(InetAddress
                    .getByName("255.255.255.255"), groupPort);
            mss.joinGroup(groupSockAddr, null);
            fail("Did not get exception when group is not a multicast address");
        } catch (IOException e) {
        }
        System.setSecurityManager(new mySecurityManager());
        try {
            group = InetAddress.getByName("224.0.0.3");
            groupSockAddr = new InetSocketAddress(group, groupPort);
            mss.joinGroup(groupSockAddr, null);
            fail("Did not get exception when joining group is not allowed");
        } catch (SecurityException e) {
        }
        System.setSecurityManager(null);
        if (atLeastOneInterface) {
            ports = Support_PortManager.getNextPortsForUDP(2);
            groupPort = ports[0];
            serverPort = ports[1];
            mss = new MulticastSocket(groupPort);
            mss.joinGroup(groupSockAddr, null);
            mss.setTimeToLive(2);
            Thread.sleep(1000);
            group = InetAddress.getByName("224.0.0.3");
            groupSockAddr = new InetSocketAddress(group, groupPort);
            server = new MulticastServer(groupSockAddr, serverPort,
                    networkInterface1);
            server.start();
            Thread.sleep(1000);
            msg = "Hello World";
            DatagramPacket sdp = new DatagramPacket(msg.getBytes(), msg
                    .length(), group, serverPort);
            mss.setTimeToLive(2);
            mss.send(sdp);
            Thread.sleep(1000);
            assertEquals("Group member did not recv data",
                         msg,
                         new String(server.rdp.getData(), 0, server.rdp.getLength()));
            server.stopServer();
            ports = Support_PortManager.getNextPortsForUDP(2);
            serverPort = ports[0];
            server = new MulticastServer(groupSockAddr, serverPort,
                    networkInterface1);
            server.start();
            Thread.sleep(1000);
            groupPort = ports[1];
            mss = new MulticastSocket(groupPort);
            InetAddress group2 = InetAddress.getByName("224.0.0.4");
            mss.setTimeToLive(10);
            msg = "Hello World - Different Group";
            sdp = new DatagramPacket(msg.getBytes(), msg.length(), group2,
                    serverPort);
            mss.send(sdp);
            Thread.sleep(1000);
            assertFalse(
                    "Group member received data when sent on different group: ",
                    new String(server.rdp.getData(), 0, server.rdp.getLength())
                            .equals(msg));
            server.stopServer();
            if (atLeastTwoInterfaces) {
                NetworkInterface loopbackInterface = NetworkInterface
                        .getByInetAddress(InetAddress.getByName("127.0.0.1"));
                boolean anyLoop = networkInterface1.equals(loopbackInterface) || networkInterface2.equals(loopbackInterface);
                ArrayList<NetworkInterface> realInterfaces = new ArrayList<NetworkInterface>();
                theInterfaces = NetworkInterface.getNetworkInterfaces();
                while (theInterfaces.hasMoreElements()) {
                    NetworkInterface thisInterface = (NetworkInterface) theInterfaces.nextElement();
                    if (thisInterface.getInetAddresses().hasMoreElements()
                            && (Support_NetworkInterface
                                    .useInterface(thisInterface) == true)){
                        realInterfaces.add(thisInterface);
                    }
                }
                for (int i = 0; i < realInterfaces.size(); i++) {
                    final int SECOND = 1;
                    NetworkInterface thisInterface = realInterfaces.get(i);
                        Enumeration<InetAddress> addresses = thisInterface.getInetAddresses();
                        NetworkInterface sendingInterface = null;
                        if (addresses.hasMoreElements()) {
                            InetAddress firstAddress = (InetAddress) addresses.nextElement();
                            if (firstAddress instanceof Inet4Address) {
                                group = InetAddress.getByName("224.0.0.4");
                                if (anyLoop) {
                                    if (networkInterface1.equals(loopbackInterface)) {
                                        sendingInterface = networkInterface2;
                                    } else {
                                        sendingInterface = networkInterface1;
                                    }
                                } else {
                                    if(i == SECOND){
                                        sendingInterface = networkInterface2;
                                    } else {
                                        sendingInterface = networkInterface1;
                                }
                               }
                            } else {
                                group = InetAddress
                                        .getByName("FF01:0:0:0:0:0:2:8001");
                                sendingInterface = IPV6networkInterface1;
                            }
                        }
                        ports = Support_PortManager.getNextPortsForUDP(2);
                        serverPort = ports[0];
                        groupPort = ports[1];
                        groupSockAddr = new InetSocketAddress(group, serverPort);
                        server = new MulticastServer(groupSockAddr, serverPort,
                                thisInterface);
                        server.start();
                        Thread.sleep(1000);
                        mss = new MulticastSocket(groupPort);
                        mss.setNetworkInterface(sendingInterface);
                        msg = "Hello World - Again" + thisInterface.getName();
                        sdp = new DatagramPacket(msg.getBytes(), msg.length(),
                                group, serverPort);
                        mss.send(sdp);
                        Thread.sleep(1000);
                        if (thisInterface.equals(sendingInterface)) {
                            assertEquals("Group member did not recv data when bound on specific interface",
                                         msg,
                                         new String(server.rdp.getData(), 0, server.rdp.getLength()));
                        } else {
                            assertFalse(
                                    "Group member received data on other interface when only asked for it on one interface: ",
                                    new String(server.rdp.getData(), 0,
                                            server.rdp.getLength()).equals(msg));
                        }
                        server.stopServer();
                    }
                groupPort = Support_PortManager.getNextPortForUDP();
                mss = new MulticastSocket(groupPort);
                mss.joinGroup(groupSockAddr, networkInterface1);
                mss.joinGroup(groupSockAddr, networkInterface2);
                try {
                    mss.joinGroup(groupSockAddr, networkInterface1);
                    fail("Did not get expected exception when joining for second time on same interface");
                } catch (IOException e) {
                }
            }
        }
		System.setSecurityManager(null);
	}
	public void test_leaveGroupLjava_net_InetAddress() {
		String msg = null;
		boolean except = false;
		InetAddress group = null;
		int[] ports = Support_PortManager.getNextPortsForUDP(2);
		int groupPort = ports[0];
		try {
			group = InetAddress.getByName("224.0.0.3");
			msg = "Hello World";
			mss = new MulticastSocket(ports[1]);
			DatagramPacket sdp = new DatagramPacket(msg.getBytes(), msg
					.length(), group, groupPort);
			mss.send(sdp, (byte) 10);
			ensureExceptionThrownIfOptionIsUnsupportedOnOS(SO_MULTICAST);
		} catch (Exception e) {
			handleException(e, SO_MULTICAST);
		}
		try {
			mss.leaveGroup(group);
		} catch (java.io.IOException e) {
			except = true;
		}
		assertTrue("Failed to throw exception leaving non-member group", except);
	}
	public void test_leaveGroupLjava_net_SocketAddressLjava_net_NetworkInterface() throws Exception {
		class mySecurityManager extends SecurityManager {
			public void checkMulticast(InetAddress address) {
				throw new SecurityException("not allowed");
			}
		}
		String msg = null;
		InetAddress group = null;
		int groupPort = Support_PortManager.getNextPortForUDP();
		SocketAddress groupSockAddr = null;
		SocketAddress groupSockAddr2 = null;
                Enumeration theInterfaces = NetworkInterface.getNetworkInterfaces();
                mss = new MulticastSocket(groupPort);
                try {
                        mss.leaveGroup(null, null);
                        fail("Did not get exception when group was null");
                } catch (IllegalArgumentException e) {
                }
                try {
                        group = InetAddress.getByName("255.255.255.255");
                        groupSockAddr = new InetSocketAddress(group, groupPort);
                        mss.leaveGroup(groupSockAddr, null);
                        fail("Did not get exception when group is not a multicast address");
                } catch (IOException e) {
                }
                System.setSecurityManager(new mySecurityManager());
                try {
                        group = InetAddress.getByName("224.0.0.3");
                        groupSockAddr = new InetSocketAddress(group, groupPort);
                        mss.leaveGroup(groupSockAddr, null);
                        fail("Did not get exception when joining group is not allowed");
                } catch (SecurityException e) {
                }
                System.setSecurityManager(null);
                if (atLeastOneInterface) {
                        groupPort = Support_PortManager.getNextPortForUDP();
                        mss = new MulticastSocket(groupPort);
                        groupSockAddr = new InetSocketAddress(group, groupPort);
                        mss.joinGroup(groupSockAddr, null);
                        mss.leaveGroup(groupSockAddr, null);
                        try {
                                mss.leaveGroup(groupSockAddr, null);
                                fail(
                                                "Did not get exception when trying to leave group that was allready left");
                        } catch (IOException e) {
                        }
                        InetAddress group2 = InetAddress.getByName("224.0.0.4");
                        groupSockAddr2 = new InetSocketAddress(group2, groupPort);
                        mss.joinGroup(groupSockAddr, networkInterface1);
                        try {
                                mss.leaveGroup(groupSockAddr2, networkInterface1);
                                fail(
                                                "Did not get exception when trying to leave group that was never joined");
                        } catch (IOException e) {
                        }
                        mss.leaveGroup(groupSockAddr, networkInterface1);
                        if (atLeastTwoInterfaces) {
                                mss.joinGroup(groupSockAddr, networkInterface1);
                                try {
                                        mss.leaveGroup(groupSockAddr, networkInterface2);
                                        fail(
                                                        "Did not get exception when trying to leave group on wrong interface joined on ["
                                                                        + networkInterface1
                                                                        + "] left on ["
                                                                        + networkInterface2 + "]");
                                } catch (IOException e) {
                                }
                        }
                }
                System.setSecurityManager(null);
	}
	public void test_sendLjava_net_DatagramPacketB() {
		String msg = "Hello World";
		InetAddress group = null;
		int[] ports = Support_PortManager.getNextPortsForUDP(2);
		int groupPort = ports[0];
		try {
			group = InetAddress.getByName("224.0.0.3");
			mss = new MulticastSocket(ports[1]);
			server = new MulticastServer(group, groupPort);
			server.start();
			Thread.sleep(200);
			DatagramPacket sdp = new DatagramPacket(msg.getBytes(), msg
					.length(), group, groupPort);
			mss.send(sdp, (byte) 10);
			Thread.sleep(1000);
			ensureExceptionThrownIfOptionIsUnsupportedOnOS(SO_MULTICAST);
		} catch (Exception e) {
			handleException(e, SO_MULTICAST);
			try {
				mss.close();
			} catch (Exception ex) {
			}
			;
			return;
		}
		mss.close();
		byte[] data = server.rdp.getData();
		int length = server.rdp.getLength();
		assertEquals("Failed to send data. Received " + length,
                             msg, new String(data, 0, length));
	}
	public void test_setInterfaceLjava_net_InetAddress() throws UnknownHostException {
		try {
			mss = new MulticastSocket();
			mss.setInterface(InetAddress.getLocalHost());
			ensureExceptionThrownIfOptionIsUnsupportedOnOS(SO_MULTICAST_INTERFACE);
		} catch (Exception e) {
			handleException(e, SO_MULTICAST_INTERFACE);
			return;
		}
		try {
			InetAddress theInterface = mss.getInterface();
			if (theInterface instanceof Inet6Address) {
				assertTrue(
						"Failed to return correct interface IPV6",
						NetworkInterface
								.getByInetAddress(mss.getInterface())
								.equals(
										NetworkInterface
												.getByInetAddress(theInterface)));
			} else {
				assertTrue("Failed to return correct interface IPV4 got:"
						+ mss.getInterface() + " excpeted: "
						+ InetAddress.getLocalHost(), mss.getInterface()
						.equals(InetAddress.getLocalHost()));
			}
			ensureExceptionThrownIfOptionIsUnsupportedOnOS(SO_MULTICAST);
		} catch (SocketException e) {
			handleException(e, SO_MULTICAST);
		}
		try {
			mss = new MulticastSocket();
			mss.setInterface(InetAddress.getByName("224.0.0.5"));
		} catch (SocketException se) {
		} catch (IOException ioe) {
			handleException(ioe, SO_MULTICAST_INTERFACE);
			return;
		}
	}
	public void test_setNetworkInterfaceLjava_net_NetworkInterface() throws IOException, InterruptedException {
		String msg = null;
		InetAddress group = null;
		int[] ports = Support_PortManager.getNextPortsForUDP(2);
		int groupPort = ports[0];
		int serverPort = ports[1];
		if (atLeastOneInterface) {
            mss = new MulticastSocket(groupPort);
            try {
                mss.setNetworkInterface(null);
                fail("No socket exception when we set then network interface with NULL");
            } catch (SocketException ex) {
            }
            groupPort = Support_PortManager.getNextPortForUDP();
            mss = new MulticastSocket(groupPort);
            mss.setNetworkInterface(networkInterface1);
            assertEquals("Interface did not seem to be set by setNeworkInterface",
                         networkInterface1, mss.getNetworkInterface());
            group = InetAddress.getByName("224.0.0.3");
            Enumeration theInterfaces = NetworkInterface.getNetworkInterfaces();
            while (theInterfaces.hasMoreElements()) {
                NetworkInterface thisInterface = (NetworkInterface) theInterfaces
                        .nextElement();
                if (thisInterface.getInetAddresses().hasMoreElements()) {
                    if ((!((InetAddress) thisInterface.getInetAddresses()
                            .nextElement()).isLoopbackAddress())
                            &&
                            (Support_NetworkInterface
                                    .useInterface(thisInterface) == true)) {
                        ports = Support_PortManager.getNextPortsForUDP(2);
                        serverPort = ports[0];
                        server = new MulticastServer(group, serverPort);
                        server.start();
                        Thread.sleep(1000);
                        groupPort = ports[1];
                        mss = new MulticastSocket(groupPort);
                        mss.setNetworkInterface(thisInterface);
                        msg = thisInterface.getName();
                        byte theBytes[] = msg.getBytes();
                        DatagramPacket sdp = new DatagramPacket(theBytes,
                                theBytes.length, group, serverPort);
                        mss.send(sdp);
                        Thread.sleep(1000);
                        String receivedMessage = new String(server.rdp
                                .getData(), 0, server.rdp.getLength());
                        assertEquals("Group member did not recv data sent on a specific interface",
                                     msg, receivedMessage);
                        server.stopServer();
                    }
                }
            }
        }
	}
	public void test_setTimeToLiveI() {
		try {
			mss = new MulticastSocket();
			mss.setTimeToLive(120);
			assertEquals("Returned incorrect 1st TTL",
                                     120, mss.getTimeToLive());
			mss.setTimeToLive(220);
			assertEquals("Returned incorrect 2nd TTL",
                                     220, mss.getTimeToLive());
			ensureExceptionThrownIfOptionIsUnsupportedOnOS(SO_MULTICAST);
		} catch (Exception e) {
			handleException(e, SO_MULTICAST);
		}
	}
	public void test_setTTLB() {
		try {
			mss = new MulticastSocket();
			mss.setTTL((byte) 120);
			assertEquals("Failed to set TTL", 120, mss.getTTL());
			ensureExceptionThrownIfOptionIsUnsupportedOnOS(SO_MULTICAST);
		} catch (Exception e) {
			handleException(e, SO_MULTICAST);
		}
	}
	public void test_ConstructorLjava_net_SocketAddress() throws Exception {	
		MulticastSocket ms = new MulticastSocket((SocketAddress) null);
        assertTrue("should not be bound", !ms.isBound() && !ms.isClosed()
                && !ms.isConnected());
        ms.bind(new InetSocketAddress(InetAddress.getLocalHost(),
                Support_PortManager.getNextPortForUDP()));
        assertTrue("should be bound", ms.isBound() && !ms.isClosed()
                && !ms.isConnected());
        ms.close();
        assertTrue("should be closed", ms.isClosed());
        ms = new MulticastSocket(new InetSocketAddress(InetAddress
                .getLocalHost(), Support_PortManager.getNextPortForUDP()));
        assertTrue("should be bound", ms.isBound() && !ms.isClosed()
                && !ms.isConnected());
        ms.close();
        assertTrue("should be closed", ms.isClosed());
        ms = new MulticastSocket(new InetSocketAddress("localhost",
                Support_PortManager.getNextPortForUDP()));
        assertTrue("should be bound", ms.isBound() && !ms.isClosed()
                && !ms.isConnected());
        ms.close();
        assertTrue("should be closed", ms.isClosed());
        boolean exception = false;
        try {
            ms = new MulticastSocket(new InetSocketAddress("unresolvedname",
                    Support_PortManager.getNextPortForUDP()));
        } catch (IOException e) {
            exception = true;
        }
        assertTrue("Expected IOException", exception);
        InetSocketAddress addr = new InetSocketAddress("0.0.0.0", 0);
        MulticastSocket s = new MulticastSocket(addr);
        assertTrue(s.getReuseAddress()); 
	}
	public void test_getLoopbackMode() {
		try {
			MulticastSocket ms = new MulticastSocket((SocketAddress) null);
			assertTrue("should not be bound", !ms.isBound() && !ms.isClosed()
					&& !ms.isConnected());
			ms.getLoopbackMode();
			assertTrue("should not be bound", !ms.isBound() && !ms.isClosed()
					&& !ms.isConnected());
			ms.close();
			assertTrue("should be closed", ms.isClosed());
			ensureExceptionThrownIfOptionIsUnsupportedOnOS(SO_USELOOPBACK);
		} catch (IOException e) {
			handleException(e, SO_USELOOPBACK);
		}
	}
	public void test_setLoopbackModeZ() {
		try {
			MulticastSocket ms = new MulticastSocket();
			ms.setLoopbackMode(true);
			assertTrue("loopback should be true", ms.getLoopbackMode());
			ms.setLoopbackMode(false);
			assertTrue("loopback should be false", !ms.getLoopbackMode());
			ms.close();
			assertTrue("should be closed", ms.isClosed());
			ensureExceptionThrownIfOptionIsUnsupportedOnOS(SO_USELOOPBACK);
		} catch (IOException e) {
			handleException(e, SO_USELOOPBACK);
		}
	}
    public void test_setLoopbackModeSendReceive() throws IOException{
        final String ADDRESS = "224.1.2.3";
        final int PORT = Support_PortManager.getNextPortForUDP();
        final String message = "Hello, world!";
        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket(PORT);
            socket.setLoopbackMode(false); 
            socket.joinGroup(InetAddress.getByName(ADDRESS));
            byte[] sendData = message.getBytes();
            DatagramPacket sendDatagram = new DatagramPacket(sendData, 0,
                    sendData.length, new InetSocketAddress(InetAddress
                            .getByName(ADDRESS), PORT));
            socket.send(sendDatagram);
            byte[] recvData = new byte[100];
            DatagramPacket recvDatagram = new DatagramPacket(recvData,
                    recvData.length);
            socket.setSoTimeout(5000); 
            socket.receive(recvDatagram);
            String recvMessage = new String(recvData, 0, recvDatagram
                    .getLength());
            assertEquals(message, recvMessage);
        }finally {
            if (socket != null)
                socket.close();
        }
    }
	public void test_setReuseAddressZ() throws Exception {
		try {
			MulticastSocket theSocket1 = null;
			MulticastSocket theSocket2 = null;
			try {
				InetSocketAddress theAddress = new InetSocketAddress(
						InetAddress.getLocalHost(), Support_PortManager
								.getNextPortForUDP());
				theSocket1 = new MulticastSocket(null);
				theSocket2 = new MulticastSocket(null);
				theSocket1.setReuseAddress(false);
				theSocket2.setReuseAddress(false);
				theSocket1.bind(theAddress);
				theSocket2.bind(theAddress);
				fail(
						"No exception when trying to connect to do duplicate socket bind with re-useaddr set to false");
			} catch (BindException e) {
			}
			if (theSocket1 != null)
				theSocket1.close();
			if (theSocket2 != null)
				theSocket2.close();
                        InetSocketAddress theAddress = new InetSocketAddress(
                                        InetAddress.getLocalHost(), Support_PortManager
                                                        .getNextPortForUDP());
                        theSocket1 = new MulticastSocket(null);
                        theSocket2 = new MulticastSocket(null);
                        theSocket1.setReuseAddress(true);
                        theSocket2.setReuseAddress(true);
                        theSocket1.bind(theAddress);
                        theSocket2.bind(theAddress);
                        if (theSocket1 != null)
				theSocket1.close();
			if (theSocket2 != null)
				theSocket2.close();
                        theAddress =
                            new InetSocketAddress(
                                    InetAddress.getLocalHost(),
                                    Support_PortManager.getNextPortForUDP());
                        theSocket1 = new MulticastSocket(null);
                        theSocket2 = new MulticastSocket(null);
                        theSocket1.bind(theAddress);
                        theSocket2.bind(theAddress);
			if (theSocket1 != null)
				theSocket1.close();
			if (theSocket2 != null)
				theSocket2.close();
			ensureExceptionThrownIfOptionIsUnsupportedOnOS(SO_REUSEADDR);
		} catch (Exception e) {
			handleException(e, SO_REUSEADDR);
		}
	}
	protected void setUp() {
		Enumeration theInterfaces = null;
		try {
			theInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (Exception e) {
		}
		if (theInterfaces != null) {
			atLeastOneInterface = false;
			while (theInterfaces.hasMoreElements()
                    && (atLeastOneInterface == false)) {
                networkInterface1 = (NetworkInterface) theInterfaces
                        .nextElement();
                if (networkInterface1.getInetAddresses().hasMoreElements() &&
                        (Support_NetworkInterface
                                .useInterface(networkInterface1) == true)) {
                    atLeastOneInterface = true;
                }
            }
			atLeastTwoInterfaces = false;
			if (theInterfaces.hasMoreElements()) {
                while (theInterfaces.hasMoreElements()
                        && (atLeastTwoInterfaces == false)) {
                    networkInterface2 = (NetworkInterface) theInterfaces
                            .nextElement();
                    if (networkInterface2.getInetAddresses().hasMoreElements()
                            &&
                            (Support_NetworkInterface
                                    .useInterface(networkInterface2) == true)) {
                        atLeastTwoInterfaces = true;
                    }
                }
            }
			Enumeration addresses;
			try {
				theInterfaces = NetworkInterface.getNetworkInterfaces();
			} catch (Exception e) {
			}
			boolean found = false;
			while (theInterfaces.hasMoreElements() && !found) {
				NetworkInterface nextInterface = (NetworkInterface) theInterfaces
						.nextElement();
				addresses = nextInterface.getInetAddresses();
				if (addresses.hasMoreElements()) {
					while (addresses.hasMoreElements()) {
						InetAddress nextAddress = (InetAddress) addresses
								.nextElement();
						if (nextAddress instanceof Inet6Address) {
							IPV6networkInterface1 = nextInterface;
							found = true;
							break;
						}
					}
				}
			}
		}
	}
	protected void tearDown() {
		if (t != null)
			t.interrupt();
		if (mss != null)
			mss.close();
		if (server != null)
			server.stopServer();
	}
}
