from datetime import date
from enum import Enum

class EventRepeatCycles(Enum):
	NEVER = 0
	DAILY = 1
	WEEKLY = 7
	BIWEEKLY = 14
	MONTHLY = -1
	YEARLY = -2

class CalendarEvent:
	def __init__(
			self,
			name: str,
			duration: str,
			color: int,
			date: date,
			repeatEvery: int):

		self.name = name
		self.duration = duration
		self.color = color
		# Date
		self.year = date.year
		self.month = date.month
		self.day = date.day

		self.Repeat = repeatEvery

	def to_json_string(self) -> str:
		return json.dumps(vars(self), default=json_serialize, indent=4)