import datetime
from google.appengine.ext import db
from SimpleXMLRPCServer import CGIXMLRPCRequestHandler
from exceptions import Exception

# persistent channel
#   contains password and creation_date
#
class Channel(db.Model):
	password = db.StringProperty()
	creation_date = db.DateTimeProperty()

# persistent message
#   contains message and creation date
#
class Message(db.Model):
	message = db.StringProperty()
	creation_date = db.DateTimeProperty()

# get channel of specified channel_id
#
def getChannel(pstr_channel_id):
	lng_channel_id = None
	try:
		lng_channel_id = long(pstr_channel_id)
	except ValueError:
		raise ValueError('Provided channel_id is not a long value: ' + pstr_channel_id)

	channel = Channel.get_by_id(lng_channel_id)
	if channel == None:
		raise ValueError('Channel: ' + pstr_channel_id + ' does not exist')
	
	return channel

# xml-rpc handler class
#
class Handler():

	# create a new channel with the specified password
	# this message will return the unique id for this channel
	#
	def createChannel(self, pstr_password):

		# delete old channels and messages
		#
		delete_date = datetime.datetime.now() - datetime.timedelta(days=7)
		query_to_delete_old_channels = db.GqlQuery("SELECT * FROM Channel " \
			+ "WHERE creation_date < :1", delete_date)
		result = query_to_delete_old_channels.fetch(100)
		for channel in result:
			child_messages_query = Message.all()
			child_messages = child_messages_query.ancestor(channel.key())
			db.delete(child_messages)
		db.delete(result)

		# create new channel
		#
		channel = Channel()
		channel.creation_date = datetime.datetime.now()
		channel.password = pstr_password
		channel.put()
		
		# return channel id as string
		#
		return str(channel.key().id())

	# send message into channel (including password for authentication)
	#
	def sendMessage(self, pstr_channel_id, pstr_password, pstr_message_text):
		# get channel
		channel = getChannel(pstr_channel_id)
		
		# verfy password
		if channel.password != pstr_password:
			raise ValueError('Invalid password for channel: ' + pstr_channel_id)

		# create and store message
		message = Message(parent=channel)
		message.message = pstr_message_text
		message.creation_date=datetime.datetime.now()
		message.put()

		# return unique message id
		return str(message.key().id())

	# check if password is correct for specified channel id
	#
	def isValid(self, pstr_channel_id, pstr_password):
		channel = getChannel(pstr_channel_id)

		if channel.password != pstr_password:
			return "false"
		else:
			return "true"

	# get last message that was sent into the specified channel
	# (note: reading does not need authentication)
	#
	def getLastMessage(self, pstr_channel_id):
		channel = getChannel(pstr_channel_id)
	
		query_messages = db.GqlQuery("SELECT * FROM Message where ANCESTOR IS :1 order by creation_date desc", channel.key())
		result = query_messages.fetch(1)
		if result == None or len(result) == 0:
			return " "
		else:
			return result[0].message

	# get a list of all available channel ids
	#
	def allChannels(self):
		return [str(channel.key().id()) for channel in Channel.all().fetch(1000)]

handler = Handler()

# register handler class for xml-rpc
#
xml_handler = CGIXMLRPCRequestHandler()
xml_handler.register_introspection_functions()
xml_handler.register_instance(handler)
xml_handler.handle_request()