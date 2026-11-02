from models import *
from calendar_events import get_events
from datetime import date
import json

def json_serialize(obj: any) -> any:
	"""
	Handles serializing non-primitive types
	"""
	if isinstance(obj, EventRepeatCycles):
		return obj.value

	raise TypeError(f"Type {type(obj)} is not JSON serializable")

def create_calendar_events_json(path: str, events: list[CalendarEvent]):
	"""
	Converts a list of CalendarEvents into a json string and writes it to a file at a provided path
	"""
	with open(path, "w") as file:
		payload = {
			"EventRepeatCycles": {},
			"CalendarEvents": list(map(lambda obj: vars(obj), events))
		}
		for cycle in EventRepeatCycles:
			payload["EventRepeatCycles"][cycle.name] = cycle.value

		json.dump(
			payload,
			file,
			default=json_serialize,
			indent=4
		)

def main():
	create_calendar_events_json("data.json", get_events())

if __name__ == "__main__":
	main()