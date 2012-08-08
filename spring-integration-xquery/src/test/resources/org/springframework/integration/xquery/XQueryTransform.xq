<employees>
	{
			for $emp in /employees/employee
			return
			<employee>
				<id>{string($emp/@id)}</id>
				<name>{string($emp/@name)}</name>
				<department>{string($emp/@name)}</department>
			</employee>
	}
</employees>