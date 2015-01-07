grafcet exemple1 {

-- @begin steps
	initial active step s1 {
		action1
		to : st1
	}

	step s2 {
		action2
		to : st2
	}

	step s3 {
		action3
		to : st3
	}

	step s4 {
		action4
		to : st4
	}

	step s5 {
		action5
		to : st5,st6
	}

	step s6 {
		action6
	}
-- @end steps

-- @begin transitions
	transition t1 {
		condition1
		to : ts1,ts2
	}

	transition t2 {
		condition2
		to : ts3,ts4
	}

	transition t3 {
		condition3
		to : ts5
	}

	transition t4 {
		condition4
		to : ts6
	}

	transition t5 {
		condition5
		to : ts7
	}
-- @end transitions

-- @begin steptotransitions
	steptotransition st1 {
		to : t1
	}

	steptotransition st2 {
		to : t2
	}

	steptotransition st3 {
		to : t3
	}

	steptotransition st4 {
		to : t4
	}

	steptotransition st5 {
		to : t4
	}

	steptotransition st6 {
		to : t5
	}
-- @end steptotransitions
	
-- @begin transitiontostep
	transitiontostep ts1 {
		to : s2
	}

	transitiontostep ts2 {
		to : s3
	}

	transitiontostep ts3 {
		to : s4
	}

	transitiontostep ts4 {
		to : s5
	}

	transitiontostep ts5 {
		to : s5
	}

	transitiontostep ts6 {
		to : s6
	}

	transitiontostep ts7 {
		to : s6
	}
-- @end transitiontostep


}
