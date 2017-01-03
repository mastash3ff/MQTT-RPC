'''setup file to make project installable via pip install'''

from setuptools import setup, find_packages
from codecs import open
from os import path

here = path.abspath(path.dirname(__file__))

with open(path.join(here,'README.rst'),encoding='utf-8') as f:
    long_description = f.read()

setup(
    name='mqtt_rpc_python',
    version='1.0',
    description = 'A RPC client/server implementation done in Python using MQTT.',
    long_description = long_description,
    url='bsheffield.com',#TODO
    author='Brandon Sheffield',
    author_email='BSheffield2008@gmail.com',
    license='MIT',
    classifiers=[
        'Development Status : 4 - Beta',

        'Intended Audience : Developers',
        'Topic :: Software Development :: Communication Tools',

        'License :: OSI Approved :: MIT License',

        'Programming Language :: Python :: 2',
        'Programming Language :: Python :: 2.7'
        ],

    keywords='mqtt rpc python client server remote procedure call calls',

    packages=find_packages(), #currently finds only mqtt_rpc_python
    install_requires = ['paho-mqtt','json-rpc','argparse'] 
    
)
