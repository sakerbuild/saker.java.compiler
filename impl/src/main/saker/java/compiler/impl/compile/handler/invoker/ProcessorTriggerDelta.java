/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package saker.java.compiler.impl.compile.handler.invoker;

import java.util.concurrent.ConcurrentSkipListSet;

import saker.build.file.path.SakerPath;

public class ProcessorTriggerDelta {
	private ConcurrentSkipListSet<SakerPath> triggeredUnitPaths = new ConcurrentSkipListSet<>();
	private boolean readResourceTriggered;

	public ProcessorTriggerDelta() {
	}

	public ConcurrentSkipListSet<SakerPath> getTriggeredUnitPaths() {
		return triggeredUnitPaths;
	}

	public boolean isReadResourceTriggered() {
		return readResourceTriggered;
	}

	public void setReadResourceTriggered(boolean readResourceTriggered) {
		this.readResourceTriggered = readResourceTriggered;
	}
}
