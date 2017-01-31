#!/bin/python

import json, sys, ipaddress, base64, requests
from xml.etree.ElementTree import Element, SubElement, Comment, tostring
from xml.dom import minidom
#from urllib import request


username = 'admin'
password = 'admin'
valid_actions = ['mirror', 'mirror-del', 'nat', 'nat-del', 'remove-all']

def retrieve_neutron_infos(ip_list, neutron_url):
    res = {}
    auth = requests.auth.HTTPBasicAuth(username, password)
    #response = urllib.request.urlopen(topology_url)
    response = requests.request("GET", neutron_url, auth = auth)

    if response.status_code != 200:
        print ("Error in retrieving Neutron Infos. Server response with code: " +str(response.getcode()))

    else:
        #json_string = response.read().decode('utf-8')
        data = json.loads(response.text)
        info_list = data['ports']['port']

        for ref_ip in ip_list:
            for i in range(0, len(info_list)):
                try:
                    if info_list[i]['fixed-ips'][0]['ip-address'] == ref_ip:
                        res[info_list[i]['mac-address']] = [ref_ip]
                except KeyError:
                    pass

    return res

def retrieve_of_ports(ip_mac_list, odl_topology_url):
    res = {}
    auth = requests.auth.HTTPBasicAuth(username, password)
    #response = urllib.request.urlopen(topology_url)
    response = requests.request("GET", odl_topology_url, auth = auth)

    if response.status_code != 200:
        print ("Error in retrieving Neutron Infos. Server response with code: " +str(response.getcode()))

    else:
        #json_string = response.read().decode('utf-8')
        data = json.loads(response.text)
        info_list = data['topology'][0]['node']

        for i in range(0, len(info_list)):
            if 'termination-point' in info_list[i]:
                for j in range(0, len(info_list[i]['termination-point'])):
                    if 'ovsdb:interface-external-ids' in info_list[i]['termination-point'][j]:
                        for k in range(0, len(info_list[i]['termination-point'][j]['ovsdb:interface-external-ids'])):
                            if info_list[i]['termination-point'][j]['ovsdb:interface-external-ids'][k]['external-id-value'] in ip_mac_list.keys():
                                ip_mac_list[info_list[i]['termination-point'][j]['ovsdb:interface-external-ids'][k]['external-id-value']].append(info_list[i]['termination-point'][j]['ovsdb:ofport'])



    return ip_mac_list

def generate_ip_mac_port(ip_mac_list):
    ip_mac_port = {}
    for key in ip_mac_list:
        ip_mac_port[ip_mac_list[key][0]] = [key, ip_mac_list[key][1]]
    return ip_mac_port

def mirror_xml(ip_mac_port, ip_list):
    flow = Element('flow')
    flow.set('xmlns', 'urn:opendaylight:flow:inventory')

    priority = SubElement(flow, 'priority')
    priority.text = '65535'

    match = SubElement(flow, 'match')

    in_port = SubElement(match, 'in-port')
    in_port.text = str(ip_mac_port[ip_list[0]][1])

    ethernet_match = SubElement(match, 'ethernet-match')
    ethernet_type = SubElement(ethernet_match, 'ethernet-type')
    e_type = SubElement(ethernet_type, 'type')
    e_type.text = '2048' #ip

    ipv4_source = SubElement(match, 'ipv4-source')
    ipv4_source.text = ip_list[0] +"/32"

    ipv4_destination = SubElement(match, 'ipv4-destination')
    ipv4_destination.text = ip_list[1] +"/32"

    flow_id = SubElement(flow, 'id')
    flow_id.text = "mirror"

    table_id = SubElement(flow, 'table_id')
    table_id.text = '0'

    instructions = SubElement(flow, 'instructions')
    instruction = SubElement(instructions, 'instruction')
    order = SubElement(instruction, 'order')
    order.text = '0'

    apply_actions = SubElement(instruction, 'apply-actions')
    action = SubElement(apply_actions, 'action')
    a_order = SubElement(action, 'order')
    a_order.text = '0'
    a_set_field = SubElement(action, 'set-field')
    a_ethernet_match = SubElement(a_set_field, 'ethernet-match')
    a_ethernet_destination = SubElement(a_ethernet_match, 'ethernet-destination')
    a_address = SubElement(a_ethernet_destination, 'address')
    a_address.text = ip_mac_port[ip_list[1]][0]

    action = SubElement(apply_actions, 'action')
    a_order = SubElement(action, 'order')
    a_order.text = '1'
    output_action = SubElement(action, 'output-action')
    max_length = SubElement(output_action, 'max-length')
    max_length.text = '65535'
    out_node_connection = SubElement(output_action, 'output-node-connector')
    out_node_connection.text = str(ip_mac_port[ip_list[1]][1])

    action = SubElement(apply_actions, 'action')
    a_order = SubElement(action, 'order')
    a_order.text = '2'
    a_set_field = SubElement(action, 'set-field')
    a_ethernet_match = SubElement(a_set_field, 'ethernet-match')
    a_ethernet_destination = SubElement(a_ethernet_match, 'ethernet-destination')
    a_address = SubElement(a_ethernet_destination, 'address')
    a_address.text = ip_mac_port[ip_list[2]][0]

    action = SubElement(apply_actions, 'action')
    a_order = SubElement(action, 'order')
    a_order.text = '3'
    output_action = SubElement(action, 'output-action')
    max_length = SubElement(output_action, 'max-length')
    max_length.text = '65535'
    out_node_connection = SubElement(output_action, 'output-node-connector')
    out_node_connection.text = str(ip_mac_port[ip_list[2]][1])

    rough_string = tostring(flow)
    reparsed = minidom.parseString(rough_string)
    print (reparsed.toprettyxml(indent="  "))

    return flow

def nat_1_xml(ip_mac_port, ip_list):
    flow = Element('flow')
    flow.set('xmlns', 'urn:opendaylight:flow:inventory')

    priority = SubElement(flow, 'priority')
    priority.text = '65535'

    match = SubElement(flow, 'match')

    in_port = SubElement(match, 'in-port')
    in_port.text = str(ip_mac_port[ip_list[0]][1])

    ethernet_match = SubElement(match, 'ethernet-match')
    ethernet_type = SubElement(ethernet_match, 'ethernet-type')
    e_type = SubElement(ethernet_type, 'type')
    e_type.text = '2048' #ip

    ipv4_destination = SubElement(match, 'ipv4-destination')
    ipv4_destination.text = ip_list[1] +"/32"

    flow_id = SubElement(flow, 'id')
    flow_id.text = "nat-1"

    table_id = SubElement(flow, 'table_id')
    table_id.text = '0'

    instructions = SubElement(flow, 'instructions')
    instruction = SubElement(instructions, 'instruction')
    order = SubElement(instruction, 'order')
    order.text = '0'

    apply_actions = SubElement(instruction, 'apply-actions')

    action = SubElement(apply_actions, 'action')
    a_order = SubElement(action, 'order')
    a_order.text = '0'
    a_set_field = SubElement(action, 'set-field')
    a_ipv4_destination = SubElement(a_set_field, 'ipv4-destination')
    a_ipv4_destination.text = ip_list[2]+"/32"

    action = SubElement(apply_actions, 'action')
    a_order = SubElement(action, 'order')
    a_order.text = '1'
    a_set_field = SubElement(action, 'set-field')
    a_ethernet_match = SubElement(a_set_field, 'ethernet-match')
    a_ethernet_destination = SubElement(a_ethernet_match, 'ethernet-destination')
    a_address = SubElement(a_ethernet_destination, 'address')
    a_address.text = ip_mac_port[ip_list[2]][0]

    action = SubElement(apply_actions, 'action')
    a_order = SubElement(action, 'order')
    a_order.text = '2'
    output_action = SubElement(action, 'output-action')
    max_length = SubElement(output_action, 'max-length')
    max_length.text = '65535'
    out_node_connection = SubElement(output_action, 'output-node-connector')
    out_node_connection.text = str(ip_mac_port[ip_list[2]][1])

    rough_string = tostring(flow)
    reparsed = minidom.parseString(rough_string)
    print (reparsed.toprettyxml(indent="  "))
    return flow

def nat_2_xml(ip_mac_port, ip_list):

    flow = Element('flow')
    flow.set('xmlns', 'urn:opendaylight:flow:inventory')

    priority = SubElement(flow, 'priority')
    priority.text = '65535'

    match = SubElement(flow, 'match')

    in_port = SubElement(match, 'in-port')
    in_port.text = str(ip_mac_port[ip_list[2]][1])

    ethernet_match = SubElement(match, 'ethernet-match')
    ethernet_type = SubElement(ethernet_match, 'ethernet-type')
    e_type = SubElement(ethernet_type, 'type')
    e_type.text = '2048' #ip

    ipv4_source = SubElement(match, 'ipv4-source')
    ipv4_source.text = ip_list[2] +"/32"

    ipv4_destination = SubElement(match, 'ipv4-destination')
    ipv4_destination.text = ip_list[0] +"/32"

    flow_id = SubElement(flow, 'id')
    flow_id.text = "nat-2"

    table_id = SubElement(flow, 'table_id')
    table_id.text = '0'

    instructions = SubElement(flow, 'instructions')
    instruction = SubElement(instructions, 'instruction')
    order = SubElement(instruction, 'order')
    order.text = '0'

    apply_actions = SubElement(instruction, 'apply-actions')

    action = SubElement(apply_actions, 'action')
    a_order = SubElement(action, 'order')
    a_order.text = '0'
    a_set_field = SubElement(action, 'set-field')
    a_ipv4_source = SubElement(a_set_field, 'ipv4-source')
    a_ipv4_source.text = ip_list[1] +"/32"

    action = SubElement(apply_actions, 'action')
    a_order = SubElement(action, 'order')
    a_order.text = '1'
    a_set_field = SubElement(action, 'set-field')
    a_ethernet_match = SubElement(a_set_field, 'ethernet-match')
    a_ethernet_source = SubElement(a_ethernet_match, 'ethernet-source')
    a_address = SubElement(a_ethernet_source, 'address')
    a_address.text = ip_mac_port[ip_list[1]][0]

    action = SubElement(apply_actions, 'action')
    a_order = SubElement(action, 'order')
    a_order.text = '2'
    a_set_field = SubElement(action, 'set-field')
    a_ethernet_match = SubElement(a_set_field, 'ethernet-match')
    a_ethernet_destination = SubElement(a_ethernet_match, 'ethernet-destination')
    a_address = SubElement(a_ethernet_destination, 'address')
    a_address.text = ip_mac_port[ip_list[0]][0]

    action = SubElement(apply_actions, 'action')
    a_order = SubElement(action, 'order')
    a_order.text = '3'
    output_action = SubElement(action, 'output-action')
    max_length = SubElement(output_action, 'max-length')
    max_length.text = '65535'
    out_node_connection = SubElement(output_action, 'output-node-connector')
    out_node_connection.text = str(ip_mac_port[ip_list[0]][1])

    rough_string = tostring(flow)
    reparsed = minidom.parseString(rough_string)
    print (reparsed.toprettyxml(indent="  "))
    return flow

def add_flow_entry(xml, url):

    xml_request = tostring(xml)
    auth = requests.auth.HTTPBasicAuth(username, password)
    headers = {'Content-Type': 'application/xml'}

    response = requests.put(url, xml_request.decode('utf-8'), auth = auth, headers = headers)
    if response.status_code == 200:
        print("SUCCESS - Flow Entry INSTALLED")
    else:
        print("ERROR - Flow Entry NOT INSTALLED - HTTP Error Code " + str(response.status_code))

def remove_flow_entry(url):
    auth = requests.auth.HTTPBasicAuth(username, password)
    headers = {'Content-Type': 'application/xml'}
    response = requests.delete(url, auth = auth, headers = headers)
    if response.status_code == 200:
        print("SUCCESS - Flow Entry REMOVED")
    elif response.status_code == 404:
        print("WARNING - Flow Entry NOT REMOVED as it does not exist")
    else:
        print("ERROR - Flow Entry NOT REMOVED - HTTP Error Code " + str(response.status_code))

def remove_all_entries(url):
    remove_flow_entry(url)

def usage():
    print("Usage: sp_config.py <action> <sdn_controller_ip> <ovs_id> <ip_1, ip2, ip3>")
    print()
    print("ADD FLOW ACTIONS: 'mirror', 'nat'")
    print("\t[mirror] - Have to specify 3 Ip Addresses: src_ip and dst_ip of flow you want mirror and sniffer_machine_ip")
    print("\t[nat] - Have to specify 3 Ip Addresses: src_ip and dst_ip of flow you want redirect and redirection_ip (ip you want to send packets instead of dst_ip")
    print("e.g: python3 flowT_0.1.py mirror sdn_controller_ip openflow:xxxxxx sender_ip, receiver_ip, sniffer_ip")
    print()
    print("REMOVE FLOW ACTIONS: 'mirror-del', 'nat-del', 'remove-all' (removes all configurations)")
    print("\tHave to specify only <action> you want to perform")
    print("e.g.: python3 flowT_0.1.py mirror-del")

def main():
    if len(sys.argv) < 4: #name, action, controller_ip, ovs_id
        usage()
    else:
        if sys.argv[1] not in valid_actions:
            print("%s is not a valid action!", sys.argv[1])
            usage()
            return
        #"http://155.54.204.86:8181/restconf/config/opendaylight-inventory:nodes/"
        neutron_url="http://"+sys.argv[2]+":8181/restconf/config/neutron:neutron/ports"
        odl_topology_url = "http://"+sys.argv[2]+":8181/restconf/operational/network-topology:network-topology/topology/ovsdb:1/"
        config_base_url = "http://"+sys.argv[2]+":8181/restconf/config/opendaylight-inventory:nodes/"
        config_url = config_base_url+"node/"+sys.argv[3]+"/table/0/flow/"
        if len(sys.argv) < 7: #name, action, controller_ip, ovs_id, ip_1,...,ip_3

            if sys.argv[1] == "remove-all":
                remove_all_entries(config_base_url)
            elif sys.argv[1] == "mirror-del":
                remove_flow_entry(config_url+"mirror")
            elif sys.argv[1] == "nat-del":
                remove_flow_entry(config_url+"nat-1")
                remove_flow_entry(config_url+"nat-2")
            else:
                usage()

        else:
            neutron_infos = retrieve_neutron_infos(sys.argv[4:], neutron_url)
            of_ports = retrieve_of_ports(neutron_infos, odl_topology_url)
            ip_mac_port = generate_ip_mac_port(of_ports)
            if sys.argv[1] == "mirror":
                m_xml = mirror_xml(ip_mac_port, sys.argv[4:])
                add_flow_entry(m_xml, config_url+"mirror")
            elif sys.argv[1] == "nat":
                n_xml = nat_1_xml(ip_mac_port, sys.argv[4:])
                add_flow_entry(n_xml, config_url+"nat-1")
                n_xml = nat_2_xml(ip_mac_port, sys.argv[4:])
                add_flow_entry(n_xml, config_url+"nat-2")
            else:
                usage()
    return


if __name__ == "__main__":
    main()
