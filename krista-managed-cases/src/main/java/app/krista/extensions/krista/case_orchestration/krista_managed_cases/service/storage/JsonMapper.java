/*
 * Krista Managed Cases Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.storage;

import java.lang.reflect.Type;
import com.google.gson.Gson;
import org.jvnet.hk2.annotations.Service;

@Service
public class JsonMapper {

    private final Gson gson;

    public JsonMapper() {
        this(new Gson());
    }

    public JsonMapper(Gson gson) {
        this.gson = gson;
    }

    public <T> T fromJson(String json, Type type) {
        return gson.fromJson(json, type);
    }

    public String toJson(Object value) {
        return gson.toJson(value);
    }

}
