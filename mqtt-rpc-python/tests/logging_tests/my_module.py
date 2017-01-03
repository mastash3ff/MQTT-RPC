import logging

logger = logging.getLogger(__name__)

def foo():
        logger.info('Hi, foo')

class Bar(object):
    def bar(self):
        logger.info('Hi, bar')
