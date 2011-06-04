class CreateAssociations < ActiveRecord::Migration
  def self.up
    create_table :associations, :id => false do |t|
      t.integer :measure_id
      t.integer :urban_id

      t.timestamps
    end
  end

  def self.down
    drop_table :associations
  end
end
