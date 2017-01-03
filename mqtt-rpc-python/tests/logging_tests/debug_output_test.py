import logging

logging.basicConfig( filename='controlflow.log', level=logging.DEBUG, filemode='w')
logger = logging.getLogger(__name__)

logger.debug("sup")
